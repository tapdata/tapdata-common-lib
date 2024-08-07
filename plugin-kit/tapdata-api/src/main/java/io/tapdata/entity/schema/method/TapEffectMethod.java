package io.tapdata.entity.schema.method;

import java.util.ArrayList;

public class TapEffectMethod extends TapMethod {
    String methodName;
    ArrayList<MethodValueEntry<?>> entries;

    public TapEffectMethod(String tapName) {
        super(tapName);
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public ArrayList<MethodValueEntry<?>> getEntries() {
        return entries;
    }

    public void setEntries(ArrayList<MethodValueEntry<?>> entries) {
        this.entries = entries;
    }

    public TapMethod withMethodName(String methodName) {
        this.methodName = methodName;
        return this;
    }


    public TapMethod witParams(ArrayList<MethodValueEntry<?>> params) {
        this.entries = params;
        return this;
    }

    public TapMethod addEntry(MethodValueEntry<?> entry) {
        if (null == entry) return this;
        if (null == this.entries) this.entries = new ArrayList<>();
        this.entries.add(entry);
        return this;
    }

    public static class MethodValueEntry<T> {
        String name;
        T value;

        public MethodValueEntry() {
        }

        public MethodValueEntry(String name, T value) {
            this.name = name;
            this.value = value;
        }

        public MethodValueEntry<T> withName(String name) {
            this.name = name;
            return this;
        }
        public MethodValueEntry<T> withValue(T value) {
            this.value = value;
            return this;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public T getValue() {
            return value;
        }

        public void setValue(T value) {
            this.value = value;
        }
    }
}
