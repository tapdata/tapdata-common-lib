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
    private volatile Map<String, Map<String, String>> capabilityAlternativeThreadMap;

    public ConnectorCapabilities alternative(String capabilityId, String alternative) {
        if (capabilityAlternativeThreadMap == null) {
            synchronized (this) {
                if (capabilityAlternativeThreadMap == null) {
                    capabilityAlternativeThreadMap = new ConcurrentHashMap<>();
                }
            }
        }
        capabilityAlternativeThreadMap.computeIfPresent(Thread.currentThread().getName(), (k, v) -> {
            v.put(capabilityId, alternative);
            return v;
        });
        capabilityAlternativeThreadMap.computeIfAbsent(Thread.currentThread().getName(), k -> {
            Map<String, String> threadMap = new ConcurrentHashMap<>();
            threadMap.put(capabilityId, alternative);
            return threadMap;
        });
        return this;
    }

    public boolean isDisabled(String capabilityId) {
        return disableCapabilities != null && disableCapabilities.contains(capabilityId);
    }

    public String getCapabilityAlternative(String capabilityId) {
        if (capabilityAlternativeThreadMap != null && capabilityAlternativeThreadMap.containsKey(Thread.currentThread().getName()))
            return capabilityAlternativeThreadMap.get(Thread.currentThread().getName()).get(capabilityId);
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
