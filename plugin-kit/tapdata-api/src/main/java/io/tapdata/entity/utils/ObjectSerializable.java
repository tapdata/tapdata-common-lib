package io.tapdata.entity.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public interface ObjectSerializable {
    byte[] fromObject(Object obj);

    byte[] fromObject(Object obj, FromObjectOptions options);

    Object toObject(byte[] data);

    /**
     * @param data     序列化数据
     * @param options  序列化配置
     * @return 反序列化对象
     */
    Object toObject(byte[] data, ToObjectOptions options);

    /**
     * @param data     序列化数据
     * @param options  序列化配置
     * @param isOffset 对象是否不检查 serialVersionUID
     * @return 反序列化对象
     * @deprecated 请使用 {@link #toObject(byte[], ToObjectOptions)} 方法替代，等同：<code>options.skipSerialVersionUID(true)</code>
     */
    @Deprecated(since = "4.10.0")
    Object toObject(byte[] data, ToObjectOptions options, Boolean isOffset);

    class FromObjectOptions {
        private boolean writeNullValue = true;
        public FromObjectOptions writeNullValue(boolean writeNullValue) {
            this.writeNullValue = writeNullValue;
            return this;
        }
        private boolean useActualMapAndList = true;
        public FromObjectOptions useActualMapAndList(boolean useActualMapAndList) {
            this.useActualMapAndList = useActualMapAndList;
            return this;
        }
        private boolean toJavaPlatform = true;
        public FromObjectOptions toJavaPlatform(boolean toJavaPlatform) {
            this.toJavaPlatform = toJavaPlatform;
            return this;
        }

        public boolean isToJavaPlatform() {
            return toJavaPlatform;
        }

        public void setToJavaPlatform(boolean toJavaPlatform) {
            this.toJavaPlatform = toJavaPlatform;
        }

        public boolean isUseActualMapAndList() {
            return useActualMapAndList;
        }

        public void setUseActualMapAndList(boolean useActualMapAndList) {
            this.useActualMapAndList = useActualMapAndList;
        }

        public boolean isWriteNullValue() {
            return writeNullValue;
        }

        public void setWriteNullValue(boolean writeNullValue) {
            this.writeNullValue = writeNullValue;
        }
    }

    class ToObjectOptions {
        private ClassLoader classLoader;
        private boolean skipSerialVersionUID = false; // 忽略所有 SerialVersionUID 检查
        private Set<String> skipSerialVersionUIDClassNames; // 忽略指定类的 SerialVersionUID 检查

        // ---------- Chain structure ----------

        public ToObjectOptions classLoader(ClassLoader classLoader) {
            this.classLoader = classLoader;
            return this;
        }

        public ToObjectOptions skipSerialVersionUID(boolean isSkip) {
            this.skipSerialVersionUID = isSkip;
            return this;
        }

        public ToObjectOptions skipSerialVersionUIDClassNames(String... skipClassNames) {
            if (null != skipClassNames) {
                if (null == skipSerialVersionUIDClassNames) {
                    skipSerialVersionUIDClassNames = new HashSet<>();
                }
                skipSerialVersionUIDClassNames.addAll(Arrays.asList(skipClassNames));
            }
            return this;
        }

        public ToObjectOptions skipSerialVersionUIDClassNames(Class<?>... skipClasses) {
            if (null != skipClasses && skipClasses.length > 0) {
                String[] classNames = new String[skipClasses.length];
                for (int i = 0; i < skipClasses.length; i++) {
                    classNames[i] = skipClasses[i].getName();
                }
                return skipSerialVersionUIDClassNames(classNames);
            }
            return this;
        }

        // ---------- setter and getter ----------

        public ClassLoader getClassLoader() {
            return classLoader;
        }

        public void setClassLoader(ClassLoader classLoader) {
            this.classLoader = classLoader;
        }

        public boolean isSkipSerialVersionUID() {
            return skipSerialVersionUID;
        }

        public void setSkipSerialVersionUID(boolean skipSerialVersionUID) {
            this.skipSerialVersionUID = skipSerialVersionUID;
        }

        public Set<String> getSkipSerialVersionUIDClassNames() {
            return skipSerialVersionUIDClassNames;
        }

        public void setSkipSerialVersionUIDClassNames(Set<String> skipSerialVersionUIDClassNames) {
            this.skipSerialVersionUIDClassNames = skipSerialVersionUIDClassNames;
        }

        // ---------- tools ----------

        public boolean isSkipSerialVersionUID(String className) {
            if (isSkipSerialVersionUID()) return true;
            if (null == skipSerialVersionUIDClassNames) return false;
            return skipSerialVersionUIDClassNames.contains(className);
        }

        public boolean isSkipSerialVersionUID(Class<?> clz) {
            return isSkipSerialVersionUID(clz.getName());
        }
    }
}
