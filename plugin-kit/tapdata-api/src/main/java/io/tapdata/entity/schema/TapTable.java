package io.tapdata.entity.schema;

import io.tapdata.entity.logger.TapLogger;
import io.tapdata.entity.schema.partition.TapPartition;
import io.tapdata.entity.schema.type.TapType;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.lang.Thread.sleep;

public class TapTable extends TapItem<TapField> {
	private static final String TAG = TapTable.class.getSimpleName();

	public TapTable() {
	}

	public TapTable(String nameAndId) {
		this(nameAndId, nameAndId);
	}

	public TapTable(String id, String name) {
		this.name = name;
		this.id = id;
	}

	private Long lastUpdate;

	public TapTable lastUpdate(Long lastUpdate) {
		this.lastUpdate = lastUpdate;
		return this;
	}

	/**
	 * Please don't add or remove on this field. If you need to add or delete, please set new one.
	 * Protect ConcurrentModification.
	 */
	private LinkedHashMap<String, TapField> nameFieldMap;

	/**
	 * For the database which don't need to create table before insert records.
	 * <p>
	 * Given the default primary keys, if user don't give the specific primary key, will use defaultPrimaryKeys as primary keys.
	 */
	private List<String> defaultPrimaryKeys;

	public TapTable defaultPrimaryKeys(String defaultPrimaryKey) {
		this.defaultPrimaryKeys = Collections.singletonList(defaultPrimaryKey);
		return this;
	}

	public TapTable defaultPrimaryKeys(List<String> defaultPrimaryKeys) {
		this.defaultPrimaryKeys = defaultPrimaryKeys;
		return this;
	}

	private List<TapIndex> indexList;

	private List<TapConstraint> constraintList;

	private String id;

	private String name;
	/**
	 * 存储引擎， innoDB
	 */
	private String storageEngine;
	/**
	 * 字符编码
	 */
	private String charset;

	private String comment;

	protected String pdkId;

	private Map<String, Object> tableAttr;

	protected Collection<String> primaryKeys;
	protected final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

	public TapTable pdkId(String pdkId) {
		this.pdkId = pdkId;
		return this;
	}

	protected String pdkGroup;

	public TapTable pdkGroup(String pdkGroup) {
		this.pdkGroup = pdkGroup;
		return this;
	}

	protected String pdkVersion;

	public TapTable pdkVersion(String pdkVersion) {
		this.pdkVersion = pdkVersion;
		return this;
	}

	protected Collection<String> logicPrimaries;

	private TapIndexEx partitionIndex;

	private String partitionMasterTableId;
	private TapPartition partitionInfo;
	private String ancestorsName;

	public boolean checkIsMasterPartitionTable() {
		return Objects.nonNull(getPartitionInfo())
				&& (Objects.isNull(getPartitionMasterTableId()) || getId().equals(getPartitionMasterTableId()));
	}

	public boolean checkIsSubPartitionTable() {
		return Objects.nonNull(getPartitionInfo())
				&& Objects.nonNull(getPartitionMasterTableId())
				&& !getId().equals(getPartitionMasterTableId());
	}

	public TapTable partitionInfo(TapPartition partitionInfo) {
		this.partitionInfo = partitionInfo;
		return this;
	}

	public TapTable partitionMasterTableId(String partitionMasterTableId) {
		this.partitionMasterTableId = partitionMasterTableId;
		return this;
	}

	public String toString() {
		return "TapTable id " + id +
				" name " + name +
				" storageEngine " + storageEngine +
				" charset " + charset +
				" number of fields " + (nameFieldMap != null ? nameFieldMap.size() : 0);
	}

	public TapTable add(TapIndex index) {
		indexCheck(index);
		readWriteLock.writeLock().lock();
		try {
			if (indexList == null) {
				indexList = new ArrayList<>();
			}
			indexList.add(index);
		} finally {
			readWriteLock.writeLock().unlock();
		}
		return this;
	}

	public TapTable add(TapField field) {
		fieldCheck(field);
		readWriteLock.writeLock().lock();
		try {
			if (nameFieldMap == null) {
				nameFieldMap = new LinkedHashMap<>();
			}
			nameFieldMap.put(field.getName(), field);
			if (field.getPos() == null) {
				field.pos(nameFieldMap.size());
			}
		} finally {
			readWriteLock.writeLock().unlock();
		}
		return this;
	}

	public TapTable add(TapConstraint constraint) {
		readWriteLock.writeLock().lock();
		try {
			if (constraintList == null) {
				constraintList = new ArrayList<>();
			}
			constraintList.add(constraint);
		} finally {
			readWriteLock.writeLock().unlock();
		}
		return this;
	}

	private void indexCheck(TapIndex index) {
		if (index == null)
			throw new IllegalArgumentException("index is null when add into table " + name);
		if (index.getIndexFields() == null || index.getIndexFields().isEmpty())
			throw new IllegalArgumentException("index fields is null or empty when add into table " + name);
	}

	private void fieldCheck(TapField field) {
		if (field == null)
			throw new IllegalArgumentException("field is null when add into table " + name);
		if (field.getName() == null)
			throw new IllegalArgumentException("field name is null when add into table " + name);
	}

	public Collection<String> primaryKeys() {
		return primaryKeys(false);
	}

	public Collection<String> primaryKeys(boolean isLogic) {
		readWriteLock.readLock().lock();
		try {
			if (isLogic) {
				if (null != logicPrimaries && !logicPrimaries.isEmpty()) return logicPrimaries;
			}
			int retry = 0;
			LinkedHashMap<String, TapField> nameFieldMapCopyRef = null;
			while (retry <= 1) {
				try {
					nameFieldMapCopyRef = new LinkedHashMap<>(this.nameFieldMap);
					break;
				} catch (ConcurrentModificationException exception) {
					retry++;
					TapLogger.warn(TAG, "Get nameFieldMap retry");
					try {
						sleep(10);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}
			}
			if (nameFieldMapCopyRef == null)
				return Collections.emptyList();

			Map<Integer, String> posPrimaryKeyName = new TreeMap<>();
			for (String key : nameFieldMapCopyRef.keySet()) {
				TapField field = nameFieldMapCopyRef.get(key);
				if (field != null && ((field.getPrimaryKey() != null && field.getPrimaryKey())
						|| (field.getPrimaryKeyPos() != null && field.getPrimaryKeyPos() > 0))) {
					posPrimaryKeyName.put(field.getPrimaryKeyPos(), field.getName());
				}
			}

			if (!posPrimaryKeyName.isEmpty())
				return posPrimaryKeyName.values();

			if (isLogic) {
				if (indexList != null) {
					List<String> uniqueCondition = new ArrayList<>();
					boolean hasCore = false;
					for (TapIndex tapIndex : indexList) {
						if ((tapIndex.getPrimary() != null && tapIndex.getPrimary()) && tapIndex.getIndexFields() != null && !tapIndex.getIndexFields().isEmpty()) {
							uniqueCondition.clear();
							for (TapIndexField indexField : tapIndex.getIndexFields()) {
								uniqueCondition.add(indexField.getName());
							}
							break;
						} else if ((tapIndex.getCoreUnique() != null && tapIndex.getCoreUnique()) && tapIndex.getIndexFields() != null && !tapIndex.getIndexFields().isEmpty()) {
							if (!hasCore) {
								hasCore = true;
								uniqueCondition.clear();
								for (TapIndexField indexField : tapIndex.getIndexFields()) {
									uniqueCondition.add(indexField.getName());
								}
							}
						} else if ((tapIndex.getUnique() != null && tapIndex.getUnique()) && tapIndex.getIndexFields() != null && !tapIndex.getIndexFields().isEmpty()) {
							if (uniqueCondition.isEmpty()) {
								for (TapIndexField indexField : tapIndex.getIndexFields()) {
									uniqueCondition.add(indexField.getName());
								}
							}
						}
					}
					return uniqueCondition;
				}
			}
		} finally {
			readWriteLock.readLock().unlock();
		}
		return Collections.emptyList();
	}

	public void refreshPrimaryKeys() {
		readWriteLock.writeLock().lock();
		primaryKeys = null;
		readWriteLock.writeLock().unlock();
	}

	public TapIndexEx getPartitionIndex() {
		return partitionIndex();
	}

	public void setPartitionIndex(TapIndexEx partitionIndex) {
		this.partitionIndex = partitionIndex;
	}

	public TapIndexEx partitionIndex() {
		readWriteLock.readLock().lock();
		try {
			if (partitionIndex != null) {
				return partitionIndex;
			}
			LinkedHashMap<String, TapField> nameFieldMapCopyRef = this.nameFieldMap;
			if (nameFieldMapCopyRef == null || nameFieldMapCopyRef.isEmpty()) {
				TapLogger.warn(TAG, "Table {} field map is empty, no partition index available. ", name);
				return null;
			}

			TapIndex bestIndex = null;

//		if (indexList != null) {
//			for (TapIndex tapIndex : indexList) {
//				if(tapIndex.getIndexFields() == null)
//					continue;
//				if((bestIndex == null || bestIndex.getIndexFields().size() > tapIndex.getIndexFields().size()))
//					bestIndex = tapIndex;
//			}
//		}
			//Use primary key only, as index field may have null value, which can not be find by AdvanceFilter.
			TapIndex primaryIndex = new TapIndex().unique(true);
			for (String key : nameFieldMapCopyRef.keySet()) {
				TapField field = nameFieldMapCopyRef.get(key);
				if (field != null && ((field.getPrimaryKey() != null && field.getPrimaryKey())
						|| (field.getPrimaryKeyPos() != null && field.getPrimaryKeyPos() > 0))) {
					primaryIndex.indexField(new TapIndexField().name(field.getName()).fieldAsc(true));
				}
			}

			if (primaryIndex.getIndexFields() != null && (bestIndex == null || bestIndex.getIndexFields().size() >= primaryIndex.getIndexFields().size())) {
				bestIndex = primaryIndex;
			}
			return bestIndex != null ? new TapIndexEx(bestIndex) : null;
		} finally {
			readWriteLock.readLock().unlock();
		}
	}

	/**
	 * @deprecated difficult to check by dataType.
	 *
	 * @param indexFields
	 * @param supportedSplitTypes
	 * @return
	 */
	private boolean checkFieldSupportSplit(List<TapIndexField> indexFields, Set<String> supportedSplitTypes) {
		for(TapIndexField field : indexFields) {
			TapField tapField = nameFieldMap.get(field.getName());
			if(tapField == null || tapField.getTapType() == null) {
				TapLogger.warn(TAG, "field {} is null or tapType is null, can not be partition index. ", field.getName());
				return false;
			}
			TapType tapType = tapField.getTapType();

		}
		return true;
	}

	@Override
	public Collection<TapField> childItems() {
		if (nameFieldMap != null)
			return nameFieldMap.values();
		return null;
	}

	public int getMaxPos() {
		if (null == nameFieldMap || nameFieldMap.size() <= 0) {
			return 0;
		}
		int max = 0;
		for (TapField tapField : nameFieldMap.values()) {
			if (null != tapField && tapField.getPos() != null && max < tapField.getPos()) {
				max = tapField.getPos();
			}
		}
		return max;
	}

	public int getMaxPKPos() {
		if (null == nameFieldMap || nameFieldMap.size() <= 0) {
			return 0;
		}
		int max = 0;
		for (TapField tapField : nameFieldMap.values()) {
			if (null != tapField.getPrimaryKeyPos() && max < tapField.getPrimaryKeyPos()) {
				max = tapField.getPrimaryKeyPos();
			}
		}
		return max;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void putField(String name, TapField field) {
		field.setName(name);
		add(field);
	}

	public LinkedHashMap<String, TapField> getNameFieldMap() {
		return nameFieldMap;
	}

	public void setNameFieldMap(LinkedHashMap<String, TapField> nameFieldMap) {
		readWriteLock.writeLock().lock();
		this.nameFieldMap = nameFieldMap;
		readWriteLock.writeLock().unlock();
	}

	public String getStorageEngine() {
		return storageEngine;
	}

	public void setStorageEngine(String storageEngine) {
		this.storageEngine = storageEngine;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<TapIndex> getIndexList() {
		return indexList;
	}

	public void setIndexList(List<TapIndex> indexList) {
		readWriteLock.writeLock().lock();
		this.indexList = indexList;
		readWriteLock.writeLock().unlock();
	}

	public List<TapConstraint> getConstraintList() {
		return constraintList;
	}

	public void setConstraintList(List<TapConstraint> constraintList) {
		readWriteLock.writeLock().lock();
		this.constraintList = constraintList;
		readWriteLock.writeLock().unlock();
	}

	public List<String> getDefaultPrimaryKeys() {
		return defaultPrimaryKeys;
	}

	public void setDefaultPrimaryKeys(List<String> defaultPrimaryKeys) {
		this.defaultPrimaryKeys = defaultPrimaryKeys;
	}

	public String getPdkId() {
		return pdkId;
	}

	public void setPdkId(String pdkId) {
		this.pdkId = pdkId;
	}

	public String getPdkGroup() {
		return pdkGroup;
	}

	public void setPdkGroup(String pdkGroup) {
		this.pdkGroup = pdkGroup;
	}

	public String getPdkVersion() {
		return pdkVersion;
	}

	public void setPdkVersion(String pdkVersion) {
		this.pdkVersion = pdkVersion;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Long getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(Long lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public void setLogicPrimaries(Collection<String> logicPrimaries) {
		this.logicPrimaries = logicPrimaries;
	}

	public Map<String, Object> getTableAttr() {
		return tableAttr;
	}

	public void setTableAttr(Map<String, Object> tableAttr) {
		this.tableAttr = tableAttr;
	}

	public String getPartitionMasterTableId() {
		return partitionMasterTableId;
	}

	public void setPartitionMasterTableId(String partitionMasterTableId) {
		this.partitionMasterTableId = partitionMasterTableId;
	}

	public TapPartition getPartitionInfo() {
		return partitionInfo;
	}

	public void setPartitionInfo(TapPartition partitionInfo) {
		this.partitionInfo = partitionInfo;
	}

	public String getAncestorsName() {
		return ancestorsName;
	}

	public void setAncestorsName(String ancestorsName) {
		this.ancestorsName = ancestorsName;
	}

	public Collection<String> getLogicPrimaries() {
		return logicPrimaries;
	}
}
