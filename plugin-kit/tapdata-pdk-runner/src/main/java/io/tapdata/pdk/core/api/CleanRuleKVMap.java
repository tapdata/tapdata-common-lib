package io.tapdata.pdk.core.api;

import io.tapdata.entity.utils.cache.KVMap;


public abstract class CleanRuleKVMap implements KVMap<Object> {
    public abstract void setKeyTTLRule(long keyTTlSeconds,String condition,CleanRuleModel cleanRuleModel) throws Exception;

}
