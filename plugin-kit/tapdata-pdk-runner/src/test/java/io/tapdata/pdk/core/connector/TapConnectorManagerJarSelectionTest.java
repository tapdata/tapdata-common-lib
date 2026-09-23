package io.tapdata.pdk.core.connector;

import io.tapdata.pdk.core.tapnode.TapNodeInstance;
import io.tapdata.pdk.core.tapnode.TapNodeClassFactory;
import io.tapdata.pdk.core.tapnode.TapNodeInfo;
import io.tapdata.pdk.core.classloader.ExternalJarManager;
import io.tapdata.pdk.apis.spec.TapNodeSpecification;
import io.tapdata.pdk.core.utils.state.StateMachine;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TapConnectorManagerJarSelectionTest {
    private TapConnectorManager manager;
    private Map<String, TapConnector> connectors;

    @TempDir
    Path tempDir;

    @AfterEach
    void clearProperties() {
        System.clearProperty("pdk_old_jar_idle_millis");
        System.clearProperty("pdk_pinned_jar_wait_millis");
    }

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() throws Exception {
        Constructor<TapConnectorManager> constructor = TapConnectorManager.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        manager = constructor.newInstance();
        Field field = TapConnectorManager.class.getDeclaredField("jarNameTapConnectorMap");
        field.setAccessible(true);
        connectors = (Map<String, TapConnector>) field.get(manager);
    }

    private TapConnector loaded(String name, String resourceId) {
        TapConnector connector = mock(TapConnector.class);
        when(connector.getJarFile()).thenReturn(new File(name.replace(".jar", "__" + resourceId + "__.jar")));
        when(connector.getState()).thenReturn(TapConnector.STATE_IDLE);
        when(connector.hasTapConnectorNodeId("postgres", "io.tapdata", "1.0-SNAPSHOT")).thenReturn(true);
        connectors.put(TapConnectorManager.downloadedJarName(name, resourceId), connector);
        return connector;
    }

    @Test
    void selectsRequestedBuildWhenTwoTimestampedJarsHaveTheSameConnectorIdentity() {
        TapConnector old = loaded("postgres-202609230902.jar", "old");
        TapConnector current = loaded("postgres-202609230907.jar", "new");
        TapNodeInstance instance = mock(TapNodeInstance.class);
        when(current.createTapConnector(anyString(), eq("test"), eq("postgres"), eq("io.tapdata"), eq("1.0-SNAPSHOT")))
                .thenReturn(instance);

        assertSame(instance, manager.createConnectorInstance("test", "postgres", "io.tapdata", "1.0-SNAPSHOT",
                "postgres-202609230907.jar", "new"));
        verify(old, never()).createTapConnector(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void keepsOldResourceAvailableWhileNewBuildIsDownloaded() {
        TapConnector old = loaded("postgres.jar", "old");
        TapConnector current = mock(TapConnector.class);
        when(current.getJarFile()).thenReturn(new File("postgres__new__.jar"));
        when(current.getState()).thenReturn(TapConnector.STATE_IDLE);
        when(current.hasTapConnectorNodeId("postgres", "io.tapdata", "1.0-SNAPSHOT")).thenReturn(true);
        TapNodeInstance instance = mock(TapNodeInstance.class);
        when(current.createTapConnector(anyString(), eq("test"), eq("postgres"), eq("io.tapdata"), eq("1.0-SNAPSHOT")))
                .thenReturn(instance);
        Thread loader = new Thread(() -> {
            try {
                Thread.sleep(200L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            connectors.put(TapConnectorManager.downloadedJarName("postgres.jar", "new"), current);
            manager.signalJarLoaded();
        });
        loader.start();

        assertSame(instance, manager.createConnectorInstance("test", "postgres", "io.tapdata", "1.0-SNAPSHOT",
                "postgres.jar", "new"));
        verify(old, never()).createTapConnector(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void hasJarDoesNotMistakeOldBuildForRequestedResource() {
        loaded("postgres.jar", "old");

        assertTrue(manager.checkTapConnectorByJarName("postgres.jar"));
        assertFalse(manager.checkTapConnectorByJarName(TapConnectorManager.downloadedJarName("postgres.jar", "new")));
    }

    @Test
    void callersWithoutJarIdentityKeepExistingBehavior() {
        TapConnector old = loaded("postgres.jar", "old");
        manager.createConnectorInstance("test", "postgres", "io.tapdata", "1.0-SNAPSHOT", null, null);
        verify(old).createTapConnector("test", "postgres", "io.tapdata", "1.0-SNAPSHOT");
    }

    @Test
    void partialJarIdentityFallsBackForLegacyDefinitions() {
        TapConnector old = loaded("postgres.jar", "null");
        TapNodeInstance instance = mock(TapNodeInstance.class);
        when(old.createTapConnector("test", "postgres", "io.tapdata", "1.0-SNAPSHOT")).thenReturn(instance);

        assertSame(instance, manager.createConnectorInstance("test", "postgres", "io.tapdata", "1.0-SNAPSHOT",
                "postgres.jar", null));
        assertEquals("postgres__null__.jar", TapConnectorManager.downloadedJarName("postgres.jar", null));
    }

    @Test
    void downloadedJarNameOnlyRemovesARealJarSuffix() {
        assertEquals("postgres.backup__rid__.jar", TapConnectorManager.downloadedJarName("postgres.backup", "rid"));
        assertEquals("postgres__rid__.jar", TapConnectorManager.downloadedJarName("postgres.jar", "rid"));
        assertEquals("postgres.jar.backup__rid__.jar", TapConnectorManager.downloadedJarName("postgres.jar.backup", "rid"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void pinnedConnectorWithoutJarFileReturnsNull() throws Exception {
        TapConnector connector = new TapConnector();
        StateMachine<String, TapConnector> stateMachine = mock(StateMachine.class);
        when(stateMachine.getCurrentState()).thenReturn(TapConnector.STATE_IDLE);
        Field stateMachineField = TapConnector.class.getDeclaredField("stateMachine");
        stateMachineField.setAccessible(true);
        stateMachineField.set(connector, stateMachine);

        assertNull(connector.createTapConnector("postgres__rid__.jar", "test", "postgres", "io.tapdata", "1.0-SNAPSHOT"));
        assertNull(connector.createTapProcessor("postgres__rid__.jar", "test", "postgres", "io.tapdata", "1.0-SNAPSHOT"));
    }

    @Test
    void unloadsOnlyIdleSupersededBuildAndReloadsItWhenPinnedAgain() throws Exception {
        String oldName = TapConnectorManager.downloadedJarName("postgres.jar", "old");
        String newName = TapConnectorManager.downloadedJarName("postgres.jar", "new");
        File oldJar = Files.createFile(tempDir.resolve(oldName)).toFile();
        File newJar = Files.createFile(tempDir.resolve(newName)).toFile();
        TapNodeSpecification spec = new TapNodeSpecification();
        spec.setId("postgres");
        spec.setGroup("io.tapdata");
        spec.setVersion("1.0-SNAPSHOT");
        TapNodeInfo info = new TapNodeInfo();
        info.setTapNodeSpecification(spec);
        TapNodeClassFactory factory = mock(TapNodeClassFactory.class);
        when(factory.getConnectorTapNodeInfos()).thenReturn(Collections.singleton(info));
        when(factory.getProcessorTapNodeInfos()).thenReturn(Collections.emptyList());
        TapConnector old = mock(TapConnector.class);
        when(old.getJarFile()).thenReturn(oldJar);
        when(old.getModificationTime()).thenReturn(1L);
        when(old.getTapNodeClassFactory()).thenReturn(factory);
        when(old.unloadIfIdle(anyLong())).thenReturn(true);
        TapConnector current = mock(TapConnector.class);
        when(current.getJarFile()).thenReturn(newJar);
        when(current.getModificationTime()).thenReturn(2L);
        TapNodeClassFactory currentFactory = mock(TapNodeClassFactory.class);
        when(currentFactory.getConnectorTapNodeInfos()).thenReturn(Collections.emptyList());
        when(currentFactory.getProcessorTapNodeInfos()).thenReturn(Collections.emptyList());
        when(current.getTapNodeClassFactory()).thenReturn(currentFactory);
        when(current.hasTapConnectorNodeId("postgres", "io.tapdata", "1.0-SNAPSHOT")).thenReturn(true);
        connectors.put(oldName, old);
        connectors.put(newName, current);

        manager.unloadIdleJars();
        assertFalse(connectors.containsKey(oldName));
        assertSame(current, connectors.get(newName));

        TapConnector reloaded = mock(TapConnector.class);
        when(reloaded.getState()).thenReturn(TapConnector.STATE_IDLE);
        TapNodeInstance expected = mock(TapNodeInstance.class);
        when(reloaded.createTapConnector(oldName, "test", "postgres", "io.tapdata", "1.0-SNAPSHOT"))
                .thenReturn(expected);
        ExternalJarManager jarManager = mock(ExternalJarManager.class);
        when(jarManager.getPath()).thenReturn(tempDir.toString());
        when(jarManager.loadJars(oldJar.getAbsolutePath())).thenAnswer(invocation -> {
            connectors.put(oldName, reloaded);
            manager.signalJarLoaded();
            return true;
        });
        Field jarManagerField = TapConnectorManager.class.getDeclaredField("externalJarManager");
        jarManagerField.setAccessible(true);
        jarManagerField.set(manager, jarManager);

        assertSame(expected, manager.createConnectorInstance("test", "postgres", "io.tapdata", "1.0-SNAPSHOT",
                "postgres.jar", "old"));
        verify(jarManager).loadJars(oldJar.getAbsolutePath());
    }

    @Test
    void pinnedWaitCanFailFastWhenConfigured() {
        System.setProperty("pdk_pinned_jar_wait_millis", "0");
        assertNull(manager.createConnectorInstance("test", "postgres", "io.tapdata", "1.0-SNAPSHOT",
                "missing.jar", "missing"));
    }

    @Test
    void idleConnectorUnloadsItsClassloaderButNotBeforeIdleDeadline() throws Exception {
        File jar = Files.createFile(tempDir.resolve("postgres__old__.jar")).toFile();
        TapConnector connector = new TapConnector();
        connector.setJarFile(jar);
        connector.start();
        connector.startLoadJar();
        URLClassLoader loader = mock(URLClassLoader.class);
        connector.loadCompleted(jar, loader, null);

        assertFalse(connector.unloadIfIdle(System.currentTimeMillis() - 1_000L));
        assertTrue(connector.unloadIfIdle(System.currentTimeMillis()));
        assertEquals(TapConnector.STATE_TERMINATED, connector.getState());
        verify(loader).close();
    }

    @Test
    @SuppressWarnings("unchecked")
    void activeConnectorIsNeverUnloaded() throws Exception {
        TapConnector connector = new TapConnector();
        StateMachine<String, TapConnector> stateMachine = mock(StateMachine.class);
        when(stateMachine.getCurrentState()).thenReturn(TapConnector.STATE_BEING_USED);
        Field field = TapConnector.class.getDeclaredField("stateMachine");
        field.setAccessible(true);
        field.set(connector, stateMachine);

        assertFalse(connector.unloadIfIdle(Long.MAX_VALUE));
        verify(stateMachine, never()).gotoState(eq(TapConnector.STATE_TERMINATED), anyString());
    }
}
