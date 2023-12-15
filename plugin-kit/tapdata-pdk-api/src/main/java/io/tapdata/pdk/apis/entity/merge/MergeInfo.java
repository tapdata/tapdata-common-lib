package io.tapdata.pdk.apis.entity.merge;

import java.util.*;

/**
 * @author samuel
 * @Description
 * @create 2022-05-27 17:02
 **/
public class MergeInfo {
	public static final String EVENT_INFO_KEY = "MERGE_INFO";
	private MergeTableProperties currentProperty;
	private List<MergeLookupResult> mergeLookupResults;
	private Integer level;
	private Set<String> sharedJoinKeys;
	private Map<String, UpdateJoinKey> updateJoinKeys = new HashMap<>();

	public MergeTableProperties getCurrentProperty() {
		return currentProperty;
	}

	public void setCurrentProperty(MergeTableProperties currentProperty) {
		this.currentProperty = currentProperty;
	}

	public List<MergeLookupResult> getMergeLookupResults() {
		return mergeLookupResults;
	}

	public void setMergeLookupResults(List<MergeLookupResult> mergeLookupResults) {
		this.mergeLookupResults = mergeLookupResults;
	}

	public MergeInfo addLookupResult(MergeLookupResult mergeLookupResult) {
		if (null == mergeLookupResults) mergeLookupResults = new ArrayList<>();
		this.mergeLookupResults.add(mergeLookupResult);
		return this;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public Set<String> getSharedJoinKeys() {
		return sharedJoinKeys;
	}

	public void setSharedJoinKeys(Set<String> sharedJoinKeys) {
		this.sharedJoinKeys = sharedJoinKeys;
	}

	public Map<String, UpdateJoinKey> getUpdateJoinKeys() {
		return updateJoinKeys;
	}

	public void addUpdateJoinKey(String id, UpdateJoinKey updateJoinKey) {
		this.updateJoinKeys.put(id, updateJoinKey);
	}

	public static class UpdateJoinKey {
		private Map<String, Object> before;
		private Map<String, Object> after;

		private Map<String, Object> parentBefore;

		public UpdateJoinKey(Map<String, Object> before, Map<String, Object> after, Map<String, Object> parentBefore) {
			this.before = before;
			this.after = after;
			this.parentBefore = parentBefore;
		}

		public Map<String, Object> getBefore() {
			return before;
		}

		public Map<String, Object> getAfter() {
			return after;
		}

		public Map<String, Object> getParentBefore() {
			return parentBefore;
		}
	}
}
