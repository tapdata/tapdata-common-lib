package io.tapdata.pdk.core.connector;

import io.tapdata.entity.logger.TapLogger;
import io.tapdata.entity.utils.DataMap;
import io.tapdata.pdk.core.classloader.ExternalJarManager;
import io.tapdata.pdk.core.executor.ExecutorsManager;
import io.tapdata.entity.memory.MemoryFetcher;
import io.tapdata.pdk.core.tapnode.TapNodeInstance;
import io.tapdata.pdk.core.tapnode.TapNodeInfo;
import io.tapdata.pdk.core.utils.CommonUtils;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Scan jar path to create TapConnector for each jar.
 *
 */
public class TapConnectorManager implements MemoryFetcher {
    private static volatile TapConnectorManager instance;
    /**
     * Key is jar file name
     * Value is TapConnector
     */
    private final Map<String, TapConnector> jarNameTapConnectorMap = new ConcurrentHashMap<>();
    private final Map<String, File> unloadedJarFiles = new ConcurrentHashMap<>();
    private final Set<String> loadingOnDemand = ConcurrentHashMap.newKeySet();
    private final Set<String> explicitLoads = ConcurrentHashMap.newKeySet();
    private final Object jarLoaded = new Object();
    private final AtomicLong jarLoadVersion = new AtomicLong();

    private ExternalJarManager externalJarManager;

    private final AtomicBoolean isStarted = new AtomicBoolean(false);

    private TapConnectorManager() {
    }

    public TapConnectorManager start(List<File> jarFiles) {
        init(jarFiles);
        return this;
    }

    public TapConnectorManager start() {
        init(null);
        return this;
    }

    public static TapConnectorManager getInstance() {
        if(instance == null) {
            synchronized (TapConnectorManager.class) {
                if(instance == null) {
                    instance = new TapConnectorManager();
//                    instance.start();
                }
            }
        }
        return instance;
    }

    public TapConnector getTapConnectorByJarName(String jarName) {
        TapConnector connector = jarNameTapConnectorMap.get(jarName);
        if (connector != null) {
            return connector;
        }
        if (isResourceTaggedJarName(jarName)) {
            return null;
        }
        return findLatestConnector(jarName);
    }

    public boolean checkTapConnectorByJarName(String jarName) {
        return jarNameTapConnectorMap.containsKey(jarName)
                || (!isResourceTaggedJarName(jarName) && findLatestConnector(jarName) != null);
    }

    public TapNodeInstance createConnectorInstance(String associateId, String pdkId, String group, String version) {
        for (TapConnector connector : latestFirstConnectors()) {
            if(connector.hasTapConnectorNodeId(pdkId, group, version)) {
                TapNodeInstance nodeInstance = connector.createTapConnector(associateId, pdkId, group, version);
                if(nodeInstance != null)
                    return nodeInstance;
            }
        }
        return null;
    }

    public TapNodeInstance createConnectorInstance(String associateId, String pdkId, String group, String version,
                                                   String fileName, String resourceId) {
        if (fileName == null || resourceId == null) {
            return createConnectorInstance(associateId, pdkId, group, version);
        }
        return createPinnedInstance(associateId, pdkId, group, version, downloadedJarName(fileName, resourceId), true);
    }

    public TapNodeInstance createProcessorInstance(String associateId, String pdkId, String group, String version,
                                                   String fileName, String resourceId) {
        if (fileName == null || resourceId == null) {
            return createProcessorInstance(associateId, pdkId, group, version);
        }
        return createPinnedInstance(associateId, pdkId, group, version, downloadedJarName(fileName, resourceId), false);
    }

    private TapNodeInstance createPinnedInstance(String associateId, String pdkId, String group, String version,
                                                 String downloadedName, boolean connectorNode) {
        long timeout = Math.max(0L, CommonUtils.getPropertyLong("pdk_pinned_jar_wait_millis", 30_000L));
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeout);
        do {
            long versionBeforeLookup = jarLoadVersion.get();
            TapConnector connector = jarNameTapConnectorMap.get(downloadedName);
            if (connector == null && unloadedJarFiles.containsKey(downloadedName)) {
                reloadUnloadedJar(downloadedName);
                connector = jarNameTapConnectorMap.get(downloadedName);
            }
            if (connector != null) {
                String state = connector.getState();
                if (TapConnector.STATE_IDLE.equals(state) || TapConnector.STATE_BEING_USED.equals(state)) {
                    TapNodeInstance instance = connectorNode
                            ? connector.createTapConnector(downloadedName, associateId, pdkId, group, version)
                            : connector.createTapProcessor(downloadedName, associateId, pdkId, group, version);
                    if (instance != null) {
                        return instance;
                    }
                }
            }
            long remaining = deadline - System.nanoTime();
            if (remaining <= 0L) {
                return null;
            }
            try {
                synchronized (jarLoaded) {
                    if (versionBeforeLookup == jarLoadVersion.get()) {
                        TimeUnit.NANOSECONDS.timedWait(jarLoaded, remaining);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        } while (true);
    }

    private void reloadUnloadedJar(String downloadedName) {
        if (externalJarManager == null || !loadingOnDemand.add(downloadedName)) return;
        try {
            File source = unloadedJarFiles.get(downloadedName);
            if (source != null && source.isFile()) {
                unloadedJarFiles.remove(downloadedName);
                try {
                    externalJarManager.loadJars(source.getAbsolutePath());
                } catch (RuntimeException e) {
                    unloadedJarFiles.putIfAbsent(downloadedName, source);
                    throw e;
                }
            }
        } finally {
            loadingOnDemand.remove(downloadedName);
            signalJarLoaded();
        }
    }

    void signalJarLoaded() {
        synchronized (jarLoaded) {
            jarLoadVersion.incrementAndGet();
            jarLoaded.notifyAll();
        }
    }

    public static String downloadedJarName(String fileName, String resourceId) {
        if (fileName == null) {
            return null;
        }
        int extension = fileName.toLowerCase(java.util.Locale.ROOT).endsWith(".jar") ? fileName.length() - 4 : -1;
        String baseName = extension < 0 ? fileName : fileName.substring(0, extension);
        return baseName + "__" + String.valueOf(resourceId) + "__.jar";
    }

    private TapConnector findLatestConnector(String jarName) {
        String expectedBaseName = convertJarFileName(jarName);
        return jarNameTapConnectorMap.values().stream()
                .filter(connector -> connector.getJarFile() != null
                        && convertJarFileName(connector.getJarFile().getName()).equals(expectedBaseName))
                .max(java.util.Comparator.<TapConnector>comparingLong(connector -> connector.getModificationTime() == null
                        ? 0L : connector.getModificationTime()))
                .orElse(null);
    }
    public TapNodeInstance createProcessorInstance(String associateId, String pdkId, String group, String version) {
        //TODO can be optimized for performance
        for (TapConnector connector : latestFirstConnectors()) {
            if (connector.hasTapProcessorNodeId(pdkId, group, version)) {
                TapNodeInstance nodeInstance = connector.createTapProcessor(associateId, pdkId, group, version);
                if (nodeInstance != null) return nodeInstance;
            }
        }
        return null;
    }

    private List<TapConnector> latestFirstConnectors() {
        return jarNameTapConnectorMap.values().stream()
                .sorted(java.util.Comparator.<TapConnector>comparingLong(connector -> connector.getModificationTime() == null
                        ? 0L : connector.getModificationTime()).reversed())
                .collect(java.util.stream.Collectors.toList());
    }

    public void releaseAssociateId(String associateId) {
        //TODO can be optimized for performance
        Collection<TapConnector> connectors = jarNameTapConnectorMap.values();
        for(TapConnector connector : connectors) {
            connector.releaseAssociateId(associateId);
        }
    }

    void unloadIdleJars() {
        long idleMillis = Math.max(0L, CommonUtils.getPropertyLong("pdk_old_jar_idle_millis", TimeUnit.MINUTES.toMillis(30)));
        long cutoff = System.currentTimeMillis() - idleMillis;
        unloadedJarFiles.entrySet().removeIf(entry -> !entry.getValue().isFile());
        for (Map.Entry<String, TapConnector> entry : jarNameTapConnectorMap.entrySet()) {
            String name = entry.getKey();
            TapConnector candidate = entry.getValue();
            if (!isResourceTaggedJarName(name) || candidate.getJarFile() == null
                    || !candidate.getJarFile().isFile() || !hasNewerBuild(candidate)) continue;
            jarNameTapConnectorMap.computeIfPresent(name, (key, current) -> {
                if (current != candidate || !current.unloadIfIdle(cutoff)) return current;
                unloadedJarFiles.put(key, current.getJarFile());
                return null;
            });
        }
    }

    private boolean hasNewerBuild(TapConnector candidate) {
        if (candidate.getModificationTime() == null) return false;
        Collection<TapNodeInfo> connectorInfos = candidate.getTapNodeClassFactory().getConnectorTapNodeInfos();
        Collection<TapNodeInfo> processorInfos = candidate.getTapNodeClassFactory().getProcessorTapNodeInfos();
        if (connectorInfos.isEmpty() && processorInfos.isEmpty()) return false;
        for (TapNodeInfo info : connectorInfos) {
            if (info.getTapNodeSpecification() == null || !jarNameTapConnectorMap.values().stream().anyMatch(other ->
                    isNewerBuild(other, candidate) && other.hasTapConnectorNodeId(
                            info.getTapNodeSpecification().getId(), info.getTapNodeSpecification().getGroup(),
                            info.getTapNodeSpecification().getVersion()))) return false;
        }
        for (TapNodeInfo info : processorInfos) {
            if (info.getTapNodeSpecification() == null || !jarNameTapConnectorMap.values().stream().anyMatch(other ->
                    isNewerBuild(other, candidate) && other.hasTapProcessorNodeId(
                            info.getTapNodeSpecification().getId(), info.getTapNodeSpecification().getGroup(),
                            info.getTapNodeSpecification().getVersion()))) return false;
        }
        return true;
    }

    private boolean isNewerBuild(TapConnector other, TapConnector candidate) {
        if (other == candidate || other.getModificationTime() == null) return false;
        return other.getModificationTime() > candidate.getModificationTime();
    }

    private void init(List<File> jarFiles) {
        if(isStarted.compareAndSet(false, true)) {
            if(jarFiles == null) {
                String path = CommonUtils.getProperty("pdk_external_jar_path", "connectors/dist");
                boolean loadNewJarAtRuntime = CommonUtils.getPropertyBool("pdk_load_new_jar_at_runtime", true);
                boolean updateJarWhenIdleAtRuntime = CommonUtils.getPropertyBool("pdk_update_jar_when_idle_at_runtime", true);
                boolean refreshLocalJars = CommonUtils.getPropertyBool("refresh_local_jars", false);
                externalJarManager = ExternalJarManager.build()
                        .withPath(path)
                        .withLoadNewJarAtRuntime(loadNewJarAtRuntime)
                        .withRefreshLocalJars(refreshLocalJars)
                        .withUpdateJarWhenIdleAtRuntime(updateJarWhenIdleAtRuntime);
            } else {
                boolean loadNewJarAtRuntime = CommonUtils.getPropertyBool("pdk_load_new_jar_at_runtime", false);
                boolean updateJarWhenIdleAtRuntime = CommonUtils.getPropertyBool("pdk_update_jar_when_idle_at_runtime", false);
                boolean refreshLocalJars = CommonUtils.getPropertyBool("refresh_local_jars", false);
                //Init as TDD purpose.
                externalJarManager = ExternalJarManager.build()
                        .withJarFiles(jarFiles)
                        .withLoadNewJarAtRuntime(loadNewJarAtRuntime)
                        .withRefreshLocalJars(refreshLocalJars)
                        .withUpdateJarWhenIdleAtRuntime(updateJarWhenIdleAtRuntime);
            }

            externalJarManager.withJarFoundListener((jarFile, firstTime) -> {
                String realJarFile = jarFile.getName();
                        if (unloadedJarFiles.containsKey(realJarFile)) return false;
                        if(firstTime || externalJarManager.isLoadNewJarAtRuntime()
                                || loadingOnDemand.contains(realJarFile) || explicitLoads.contains(realJarFile)) {
                            TapConnector existingTapConnector = jarNameTapConnectorMap.get(realJarFile);
                            if(existingTapConnector == null) {
                                TapConnector tapConnector = new TapConnector();
                                tapConnector.setJarFile(jarFile);
                                TapConnector old = jarNameTapConnectorMap.putIfAbsent(realJarFile, tapConnector);
                                if(old == null) {
                                    tapConnector.start();
                                    tapConnector.startLoadJar();
                                    return true;
                                } else {
                                    //Another thread insert before here, do nothing here as another thread may have done the similar logic.
//                                existingTapConnector = old;
                                    return false;
                                }
                            } else {
                                if(externalJarManager.isUpdateJarWhenIdleAtRuntime() &&
                                        existingTapConnector.getState().equals(TapConnector.STATE_IDLE) &&
                                        existingTapConnector.getModificationTime() < jarFile.lastModified()) {
                                    return existingTapConnector.willUpdateJar(jarFile);
                                } else {
                                    return false;
                                }
                            }
                        }
                        return false;
                    })
                    .withJarLoadCompletedListener((jarFile, classLoader, throwable) -> {
                        TapConnector existingTapConnector = jarNameTapConnectorMap.get(jarFile.getName());
                        if(existingTapConnector != null) {
                            existingTapConnector.loadCompleted(jarFile, classLoader, throwable);
                        }
                        signalJarLoaded();
                    })
                    .withJarAnnotationHandlersListener((jarFile) -> {
                        TapConnector existingTapConnector = jarNameTapConnectorMap.get(jarFile.getName());
                        if(existingTapConnector != null)
                            return existingTapConnector.getTapNodeClassFactory().getClassAnnotationHandlers();
                        return null;
                    }).start();
            int sweepSeconds = Math.max(60, CommonUtils.getPropertyInt("pdk_old_jar_sweep_seconds", 300));
            ExecutorsManager.getInstance().getScheduledExecutorService().scheduleWithFixedDelay(() -> {
                try {
                    unloadIdleJars();
                } catch (Throwable error) {
                    TapLogger.warn(TapConnectorManager.class.getSimpleName(), "Idle jar sweep failed: {}", error.getMessage());
                }
            }, sweepSeconds, sweepSeconds, TimeUnit.SECONDS);
            ;
        }
    }

    //mongodb-connector-v1.0-SNAPSHOT__628daf0716419763bbdce3f1__.jar => mongodb-connector-v1.0-SNAPSHOT.jar
    private String convertJarFileName(String jarFileName) {
        final String separator = "__";
        int lastOne = jarFileName.lastIndexOf(separator);
        if(lastOne >= 0) {
            lastOne -= 2;
            int lastLastOne = jarFileName.lastIndexOf(separator, lastOne);
            if(lastLastOne >= 0) {
                return jarFileName.substring(0, lastLastOne) + jarFileName.substring(lastOne + 4);
            }
        }
        return jarFileName;
    }

    private boolean isResourceTaggedJarName(String jarFileName) {
        return jarFileName != null && !jarFileName.equals(convertJarFileName(jarFileName));
    }

    /**
     * Refresh local jars to discovery new or updated jars immediately
     */
    public void refreshJars(String oneJarPath) {
        if (oneJarPath == null) {
            externalJarManager.loadJars(null);
            return;
        }
        String name = new File(oneJarPath).getName();
        unloadedJarFiles.remove(name);
        explicitLoads.add(name);
        try {
            externalJarManager.loadJars(oneJarPath);
        } finally {
            explicitLoads.remove(name);
            signalJarLoaded();
        }
    }

    public static void main(String... args) {
        TapConnectorManager.getInstance().start();

        ExecutorsManager.getInstance().getScheduledExecutorService().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                System.out.println("");
//                System.out.println("GC " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 + "KB");
//                System.gc();
//                System.out.println("GCed " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 + "KB");
            }
        }, 10, 10, TimeUnit.SECONDS);
    }

    @Override
    public DataMap memory(String keyRegex, String memoryLevel) {
        DataMap dataMap = DataMap.create().keyRegex(keyRegex)/*.prefix(this.getClass().getSimpleName())*/
                .kv("isStarted", isStarted)
                .kv("ExternalJarManager", externalJarManager != null ? externalJarManager.memory(keyRegex, memoryLevel) : null);

        for(Map.Entry<String, TapConnector> entry : jarNameTapConnectorMap.entrySet()) {
            dataMap.kv(entry.getKey(), entry.getValue().memory(keyRegex, memoryLevel));
        }
        return dataMap;
    }
}
