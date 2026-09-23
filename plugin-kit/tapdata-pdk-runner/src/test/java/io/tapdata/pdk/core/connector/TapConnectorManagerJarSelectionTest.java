package io.tapdata.pdk.core.connector;

import io.tapdata.pdk.core.tapnode.TapNodeInstance;
import io.tapdata.pdk.core.utils.state.StateMachine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TapConnectorManagerJarSelectionTest {
    private TapConnectorManager manager;
    private Map<String, TapConnector> connectors;

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
}
