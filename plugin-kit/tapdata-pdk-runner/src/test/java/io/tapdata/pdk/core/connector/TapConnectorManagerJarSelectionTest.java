package io.tapdata.pdk.core.connector;

import io.tapdata.pdk.core.tapnode.TapNodeInstance;
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
        when(connector.hasTapConnectorNodeId("postgres", "io.tapdata", "1.0-SNAPSHOT")).thenReturn(true);
        connectors.put(name, connector);
        return connector;
    }

    @Test
    void selectsRequestedBuildWhenTwoTimestampedJarsHaveTheSameConnectorIdentity() {
        TapConnector old = loaded("postgres-202609230902.jar", "old");
        TapConnector current = loaded("postgres-202609230907.jar", "new");
        TapNodeInstance instance = mock(TapNodeInstance.class);
        when(current.createTapConnector("test", "postgres", "io.tapdata", "1.0-SNAPSHOT")).thenReturn(instance);

        assertSame(instance, manager.createConnectorInstance("test", "postgres", "io.tapdata", "1.0-SNAPSHOT",
                "postgres-202609230907.jar", "new"));
        verify(old, never()).createTapConnector(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void rejectsOldResourceWhenRefreshOfSameFileNameWasSkipped() {
        TapConnector old = loaded("postgres.jar", "old");
        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> manager.createConnectorInstance("test", "postgres", "io.tapdata", "1.0-SNAPSHOT",
                        "postgres.jar", "new"));
        assertTrue(error.getMessage().contains("postgres__new__.jar"));
        verify(old, never()).createTapConnector(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void missingRequestedBuildDoesNotFallBackToOldBuild() {
        TapConnector old = loaded("postgres-old.jar", "old");
        assertThrows(IllegalStateException.class,
                () -> manager.createConnectorInstance("test", "postgres", "io.tapdata", "1.0-SNAPSHOT",
                        "postgres-new.jar", "new"));
        verify(old, never()).createTapConnector(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void callersWithoutJarIdentityKeepExistingBehavior() {
        TapConnector old = loaded("postgres.jar", "old");
        manager.createConnectorInstance("test", "postgres", "io.tapdata", "1.0-SNAPSHOT", null, null);
        verify(old).createTapConnector("test", "postgres", "io.tapdata", "1.0-SNAPSHOT");
    }
}
