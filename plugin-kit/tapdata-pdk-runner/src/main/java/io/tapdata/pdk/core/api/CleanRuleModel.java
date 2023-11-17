package io.tapdata.pdk.core.api;

public enum CleanRuleModel {
        FUZZY_MATCHING("fuzzyMatching");

        public String getName() {
            return name;
        }

        private String name;

        CleanRuleModel(String name) {
            this.name = name;
        }
    }