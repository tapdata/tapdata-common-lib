package io.tapdata.entity.codec.filter;

import java.util.*;

// Numeric entries with explicit binaryFloat declarations, copied from tapdata-connectors mysql-spec.json.
// Original snapshot commit: 16aafdf01eb75dfd9b1d115b2e1ea92595091d62
final class MysqlNumberSpecFixture {
    static Map<String, Object> entry(String expression) {
        Map<String, Object> entry = new LinkedHashMap<>();
        switch (expression) {
            case "decimal[($precision,$scale)][unsigned]":
                entry.put("to", "TapNumber");
                entry.put("precision", Arrays.asList(1, 65));
                entry.put("scale", Arrays.asList(0, 30));
                entry.put("defaultPrecision", 10);
                entry.put("defaultScale", 0);
                entry.put("unsigned", "unsigned");
                entry.put("fixed", true);
                break;
            case "float":
                entry.put("to", "TapNumber");
                entry.put("precision", Arrays.asList(1, 6));
                entry.put("scale", Arrays.asList(0, 6));
                entry.put("fixed", false);
                entry.put("binaryFloat", true);
                break;
            case "double":
                entry.put("to", "TapNumber");
                entry.put("precision", Arrays.asList(1, 17));
                entry.put("preferPrecision", 11);
                entry.put("preferScale", 4);
                entry.put("scale", Arrays.asList(0, 17));
                entry.put("fixed", false);
                entry.put("binaryFloat", true);
                break;
            case "float($precision,$scale)[unsigned]":
                entry.put("to", "TapNumber");
                entry.put("name", "float");
                entry.put("precision", Arrays.asList(1, 30));
                entry.put("scale", Arrays.asList(0, 30));
                entry.put("value", Arrays.asList("-3.402823466E+38", "3.402823466E+38"));
                entry.put("unsigned", "unsigned");
                entry.put("fixed", false);
                entry.put("binaryFloat", true);
                break;
            case "double[($precision,$scale)][unsigned]":
                entry.put("to", "TapNumber");
                entry.put("precision", Arrays.asList(1, 255));
                entry.put("scale", Arrays.asList(0, 30));
                entry.put("value", Arrays.asList("-1.7976931348623157E+308", "1.7976931348623157E+308"));
                entry.put("unsigned", "unsigned");
                entry.put("fixed", false);
                entry.put("binaryFloat", true);
                break;
            case "int[($zerofill)]":
                entry.put("to", "TapNumber");
                entry.put("bit", 32);
                entry.put("precision", 10);
                entry.put("value", Arrays.asList(-2147483648, 2147483647));
                break;
            case "bigint[($zerofill)]":
                entry.put("to", "TapNumber");
                entry.put("bit", 64);
                entry.put("precision", 19);
                entry.put("value", Arrays.asList(-9223372036854775808L, 9223372036854775807L));
                break;
            default: throw new IllegalArgumentException(expression);
        }
        return entry;
    }
}
