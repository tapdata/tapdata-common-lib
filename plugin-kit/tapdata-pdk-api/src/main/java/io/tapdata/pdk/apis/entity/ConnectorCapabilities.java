package io.tapdata.pdk.apis.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectorCapabilities {
    public static ConnectorCapabilities create() {
        return new ConnectorCapabilities();
    }

    private List<String> disableCapabilities;

    public ConnectorCapabilities disable(String capabilityId) {
        if (disableCapabilities == null)
            disableCapabilities = new ArrayList<>();
        disableCapabilities.add(capabilityId);
        return this;
    }

    private Map<String, String> capabilityAlternativeMap;
    private Map<String, Map<String, String>> capabilityAlternativeThreadMap;

    public ConnectorCapabilities alternative(String capabilityId, String alternative) {
        synchronized (this) {
            if (capabilityAlternativeMap == null) {
                capabilityAlternativeMap = new ConcurrentHashMap<>();
                capabilityAlternativeMap.put(capabilityId, alternative);
            }
            if (capabilityAlternativeThreadMap == null) {
                capabilityAlternativeThreadMap = new ConcurrentHashMap<>();
            }
        }
        if (capabilityAlternativeThreadMap.containsKey(Thread.currentThread().getName())) {
            capabilityAlternativeThreadMap.get(Thread.currentThread().getName()).put(capabilityId, alternative);
        } else {
            Map<String, String> threadMap = new ConcurrentHashMap<>();
            threadMap.put(capabilityId, alternative);
            capabilityAlternativeThreadMap.put(Thread.currentThread().getName(), threadMap);
        }
        return this;
    }

    public boolean isDisabled(String capabilityId) {
        return disableCapabilities != null && disableCapabilities.contains(capabilityId);
    }

    public String getCapabilityAlternative(String capabilityId) {
        if (capabilityAlternativeThreadMap != null && capabilityAlternativeThreadMap.containsKey(Thread.currentThread().getName()))
            return capabilityAlternativeThreadMap.get(Thread.currentThread().getName()).get(capabilityId);
        if (capabilityAlternativeMap != null)
            return capabilityAlternativeMap.get(capabilityId);
        return null;
    }

    public Map<String, String> getCapabilityAlternativeMap() {
        return capabilityAlternativeMap;
    }

    public void setCapabilityAlternativeMap(Map<String, String> capabilityAlternativeMap) {
        this.capabilityAlternativeMap = capabilityAlternativeMap;
    }

    public List<String> getDisableCapabilities() {
        return disableCapabilities;
    }

    public void setDisableCapabilities(List<String> disableCapabilities) {
        this.disableCapabilities = disableCapabilities;
    }
}
