package io.tapdata.pdk.apis.entity.merge;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author samuel
 * @Description
 * @create 2022-05-27 16:18
 **/
public class MergeLookupResult implements Serializable {
	private static final long serialVersionUID = -6100854075182627105L;
	private MergeTableProperties property;
	private Map<String, Object> data;

	private boolean dataExists = true;

	private List<MergeLookupResult> mergeLookupResults;

	private Set<String> sharedJoinKeys;

	public MergeTableProperties getProperty() {
		return property;
	}

	public void setProperty(MergeTableProperties property) {
		this.property = property;
	}

	public Map<String, Object> getData() {
		return data;
	}

	public void setData(Map<String, Object> data) {
		this.data = data;
	}

	public List<MergeLookupResult> getMergeLookupResults() {
		return mergeLookupResults;
	}

	public void setMergeLookupResults(List<MergeLookupResult> mergeLookupResults) {
		this.mergeLookupResults = mergeLookupResults;
	}

	public boolean isDataExists() {
		return dataExists;
	}

	public void setDataExists(boolean dataExists) {
		this.dataExists = dataExists;
	}

	public Set<String> getSharedJoinKeys() {
		return sharedJoinKeys;
	}

	public void setSharedJoinKeys(Set<String> sharedJoinKeys) {
		this.sharedJoinKeys = sharedJoinKeys;
	}
}
