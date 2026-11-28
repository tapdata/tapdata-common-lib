package io.tapdata.entity.codec.impl;

import io.tapdata.entity.schema.value.TapNumberValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.tools.ToolProvider;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class TapNumberBinaryCompatibilityTest {
    @TempDir Path temp;

    @Test void runsUnrecompiledDoubleClientAgainstNewApi() throws Exception {
        Path oldApi = temp.resolve("TapNumberValue.java");
        try (java.io.InputStream source = getClass().getResourceAsStream("/compat/TapNumberValue.java.txt")) {
            assertNotNull(source);
            Files.copy(source, oldApi);
        }
        Path client = temp.resolve("LegacyNumberClient.java");
        Files.write(client, ("import io.tapdata.entity.schema.value.*;"
                + "public class LegacyNumberClient { public static Double read(TapNumberValue n) { return n.getValue(); } public static TapNumberValue run() {"
                + "TapNumberValue n = new TapNumberValue(1.25d);"
                + "Double d = n.getValue(); if (d != 1.25d) throw new AssertionError();"
                + "n.setValue(2.5d); if(n.getValue() != 2.5d) throw new AssertionError();"
                + "n.value(3.75d); TapValue<Double, ?> base = n;"
                + "if(base.getValue() != 3.75d) throw new AssertionError();"
                + "base.setValue(null); if(n.getValue() != null) throw new AssertionError();"
                + "base.value(4.5d); return n; }}").getBytes(StandardCharsets.UTF_8));
        assertEquals(0, ToolProvider.getSystemJavaCompiler().run(null, null, null,
                "-classpath", System.getProperty("java.class.path"), "-d", temp.toString(),
                oldApi.toString(), client.toString()));
        // Parent-first loading deliberately resolves TapNumberValue to the NEW API.
        try (URLClassLoader loader = new URLClassLoader(new URL[]{temp.toUri().toURL()}, getClass().getClassLoader())) {
            Object result = loader.loadClass("LegacyNumberClient").getMethod("run").invoke(null);
            assertSame(TapNumberValue.class, result.getClass());
            assertEquals(4.5d, ((TapNumberValue) result).getValue());
            assertEquals(10d, loader.loadClass("LegacyNumberClient").getMethod("read", TapNumberValue.class)
                    .invoke(null, TapNumberValue.ofNumber(java.math.BigDecimal.TEN)));
        }
    }
    @Test void nativeJavaStreamsRemainReadableInBothVersions() throws Exception {
        Path oldApi = temp.resolve("TapNumberValue.java");
        try (java.io.InputStream source = getClass().getResourceAsStream("/compat/TapNumberValue.java.txt")) {
            Files.copy(source, oldApi);
        }
        assertEquals(0, ToolProvider.getSystemJavaCompiler().run(null, null, null,
                "-classpath", System.getProperty("java.class.path"), "-d", temp.toString(), oldApi.toString()));
        String name = TapNumberValue.class.getName();
        try (URLClassLoader oldLoader = new URLClassLoader(new URL[]{temp.toUri().toURL()}, getClass().getClassLoader()) {
            @Override protected Class<?> loadClass(String n, boolean resolve) throws ClassNotFoundException {
                if (!n.equals(name)) return super.loadClass(n, resolve);
                Class<?> c = findLoadedClass(n);
                if (c == null) c = findClass(n);
                if (resolve) resolveClass(c);
                return c;
            }
        }) {
            Class<?> oldClass = oldLoader.loadClass(name);
            assertEquals(java.io.ObjectStreamClass.lookup(oldClass).getSerialVersionUID(),
                    java.io.ObjectStreamClass.lookup(TapNumberValue.class).getSerialVersionUID());
            Object oldValue = oldClass.getConstructor(Double.class).newInstance(1.25d);
            for (Object input : new Object[]{oldValue, new TapNumberValue(1.25d),
                    TapNumberValue.ofNumber(new java.math.BigDecimal("9007199254740993.00"))}) {
                java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream();
                try (java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(bytes)) { out.writeObject(input); }
                boolean toNew = input == oldValue;
                try (java.io.ObjectInputStream in = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bytes.toByteArray())) {
                    @Override protected Class<?> resolveClass(java.io.ObjectStreamClass desc) throws java.io.IOException, ClassNotFoundException {
                        return desc.getName().equals(name) ? (toNew ? TapNumberValue.class : oldClass) : super.resolveClass(desc);
                    }
                }) {
                    Object result = in.readObject();
                    assertSame(toNew ? TapNumberValue.class : oldClass, result.getClass());
                    // Historical Java streams never included the inherited Double payload.
                    assertNull(result.getClass().getMethod("getValue").invoke(result));
                }
            }
        }
    }

}
