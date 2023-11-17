package io.tapdata.pdk.core.utils;

import io.tapdata.entity.utils.cache.KVMap;
import io.tapdata.pdk.core.api.CleanRuleKVMap;

public class CleanRuleKVMapUtils {
    public static CleanRuleKVMap getCleanruleKVMap(KVMap<Object> kvMap){
        return (CleanRuleKVMap) kvMap;
    }
}
