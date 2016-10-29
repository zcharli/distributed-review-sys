package core;

import com.fasterxml.jackson.databind.ObjectMapper;
import config.DHTConfig;
import exceptions.InitializationFailedException;
import metrics.ConcurrentTrackingList;
import metrics.MetricsCollector;
import metrics.TrackingContext;
import metrics.Tuple;
import msg.RedisElementContainer;
import net.tomp2p.connection.Bindings;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDiscover;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.peers.PeerAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Created by czl on 20/09/16.
 */
public class DHTProfile {

    public static enum StorageTypes {
        DHT,
        METRIC,
        KEYSTORE
    }

    private static DHTProfile INSTANCE;

    private final static Logger LOGGER = LoggerFactory.getLogger(DHTProfile.class);

    private final ObjectMapper objectMapper;

    public final PeerDHT MY_PROFILE;

    private final MetricsCollector m_metricsCollector;

    private final Set<Number160> m_keystore;

    private DHTProfile(boolean isBootStrap, boolean isPersistent) throws InitializationFailedException {
        m_keystore = new ConcurrentSkipListSet<>();
        PeerDHT currentClient = null;
        MetricsCollector metrics = null;
        objectMapper = new ObjectMapper();
        try {
            // OffHeapStorage can be configured for versions and version check intervals
            Map<StorageTypes, List<Object>> persistedData = loadDataFromStorage();
            OffHeapStorage storageLayer = isPersistent ? new OffHeapStorage()
                    .setLoadedValues(persistedData.containsKey(StorageTypes.DHT)
                            ? persistedData.get(StorageTypes.DHT) : new LinkedList<>()) : null;
            metrics = new MetricsCollector(persistedData.containsKey(StorageTypes.METRIC)
                    ? persistedData.get(StorageTypes.METRIC) : new LinkedList<Object>());

            if (persistedData.containsKey(StorageTypes.KEYSTORE)) {
                for (Object keys : persistedData.get(StorageTypes.KEYSTORE)) {
                    try {
                        Number160 castedKeys = (Number160) keys;
                        m_keystore.add(castedKeys);
                    } catch (Exception e) {
                        LOGGER.error("Unabled to cast up Num160 keys for keystore during load");
                    }
                }
            }

            Bindings b = new Bindings();
            // TODO: Make add interface set-able on boot up
            b.addInterface(DHTConfig.DHT_LISTEN_INTERFACE);
            if (isBootStrap) {
                Random r = new Random(42L);
                b.addAddress(DHTConfig.instance().BOOTSRAP_ADDR);
                currentClient = new PeerBuilderDHT(new PeerBuilder(new Number160(r)).bindings(b).ports(DHTConfig.DRS_PORT).start())
                        .storage(storageLayer)
                        .start();
            } else {
                Random r = new Random(43L);
                System.out.println("Trying to connect to: " + DHTConfig.instance().BOOTSRAP_ADDR.getHostAddress() + ":" + DHTConfig.DRS_PORT);
                currentClient = new PeerBuilderDHT(new PeerBuilder(new Number160(r)).ports(DHTConfig.DRS_PORT).bindings(b).behindFirewall().start())
                        .storage(storageLayer)
                        .start();
            }
        } catch (Exception e) {
            LOGGER.error("DHTProfile failed to activate peer.");
            e.printStackTrace();
        } finally {
            MY_PROFILE = currentClient;
            m_metricsCollector = metrics;
        }
    }

    public Set<Number160> getKeyStore() {
        return m_keystore;
    }

    public MetricsCollector getMetricsCollector() {
        return m_metricsCollector;
    }

    public Map<StorageTypes, List<Object>> loadDataFromStorage() {
        Map<StorageTypes, List<Object>> fromDisk = new HashMap<>();
        try (Jedis adapter = DHTConfig.REDIS_RESOURCE_POOL.getResource()) {
            Set<String> allKeys = adapter.keys("*");
            for (String key : allKeys) {
                if (key.startsWith("drs")) {
                    if (!fromDisk.containsKey(StorageTypes.DHT)) {
                        fromDisk.put(StorageTypes.DHT, new LinkedList<>());
                    }
                    for (String jsonData : adapter.lrange(key, 0, -1)) {
                        try {
                            RedisElementContainer data = objectMapper.readValue(jsonData, RedisElementContainer.class);
                            Number640 mapKey = new Number640(
                                    new Number160(data.getLocationBuffer())
                                    , new Number160(data.getDomainBuffer())
                                    , new Number160(data.getContentBuffer())
                                    , new Number160(data.getVersionBuffer()));
                            Tuple<Number640, RedisElementContainer> entry = new Tuple<>(mapKey, data);
                            fromDisk.get(StorageTypes.DHT).add(entry);
                            LOGGER.debug("Loaded a element from disk: " + mapKey.toString());
                        } catch (IOException e) {
                            LOGGER.error("Failed to deserialize data json: " + e.getMessage());
                        }
                    }
                } else if (key.startsWith(DHTConfig.TRACKED_ID)) {
                    if (!fromDisk.containsKey(StorageTypes.METRIC)) {
                        fromDisk.put(StorageTypes.METRIC, new LinkedList<>());
                    }
                    String[] split = key.split(":");
                    if (split.length < 3) {
                        LOGGER.error("Location key found less than 3 parts. Invalid, required trk:loc:id...");
                        continue;
                    }
                    Number160 locationKey = new Number160(split[2]);
                    for (String jsonData : adapter.lrange(key, 0, -1)) {
                        try {
                            TrackingContext[] fromBuffer = objectMapper.readValue(jsonData, TrackingContext[].class);
                            Number160 keyFromBuffer = locationKey;
                            Tuple<Number160, TrackingContext[]> entry = new Tuple<>(keyFromBuffer, fromBuffer);
                            fromDisk.get(StorageTypes.METRIC).add(entry);
                        } catch (Exception e) {
                            LOGGER.error("Error reading in load tracked data: " + e.getMessage());
                        }
                    }
                } else if (key.equals(DHTConfig.KEYSTORE_ADDR)) {
                    if (!fromDisk.containsKey(StorageTypes.KEYSTORE)) {
                        fromDisk.put(StorageTypes.KEYSTORE, new LinkedList<>());
                    }

                    Set<String> serializedIntArrays = adapter.hkeys(DHTConfig.KEYSTORE_ADDR);
                    for (String number160Buffer : serializedIntArrays) {
                        try {
                            int[] buffer = objectMapper.readValue(number160Buffer, int[].class);
                            Number160 locationKey = new Number160(buffer);
                            fromDisk.get(StorageTypes.KEYSTORE).add(locationKey);
                        } catch (Exception e) {
                            LOGGER.error("An error occured when trying to deserialize Number160 buffer: " + e.getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Redis was not found on the system. At the present time, on memory storage is not supported.");
            e.printStackTrace();
            System.exit(0);
        }
        LOGGER.debug("Finished loading elements from disk.");
        return fromDisk;
    }

    public static DHTProfile instance()  {
        if (INSTANCE == null) {
            LOGGER.warn("DHTProfile.init(isBootstrap) must be called first");
            return null;
        }
        return INSTANCE;
    }

    private void connectToBootstrapServer() throws InitializationFailedException {
        PeerAddress bootStrapServer = new PeerAddress(Number160.ZERO, DHTConfig.instance().BOOTSRAP_ADDR, DHTConfig.DRS_PORT,DHTConfig.DRS_PORT,DHTConfig.DRS_PORT+1);
        FutureDiscover fd = MY_PROFILE.peer().discover().peerAddress(bootStrapServer).start();
        fd.awaitUninterruptibly();

        if (!fd.isSuccess()) {
            LOGGER.error("Unable to find profile's outside address");
            throw new InitializationFailedException("DHTProfile failed it's discovery phase: " + fd.failedReason());
        } else {
            LOGGER.info("Client successfully discovered bootstrap server.");
        }

        bootStrapServer = fd.reporter();
        FutureBootstrap bootstrap = MY_PROFILE.peer().bootstrap().peerAddress(bootStrapServer).start();
        bootstrap.awaitUninterruptibly();
        if (!bootstrap.isSuccess()) {
            LOGGER.error("Unable to find bootstrap this client.");
            throw new InitializationFailedException("DHTProfile failed it's bootstrap phase: " + bootstrap.failedReason());
        } else {
            LOGGER.info("Client successfully connected to bootstrap server.");
        }
    }

    /**
     * Should only be called once, possible bug here.
     * @param isBootstrap
     * @return
     * @throws InitializationFailedException
     */
    public static DHTProfile init(boolean isBootstrap, boolean isPersistent) throws InitializationFailedException {
        if (INSTANCE != null) {
            return INSTANCE;
        }

        INSTANCE = new DHTProfile(isBootstrap, isPersistent);
        if (!isBootstrap) {
            INSTANCE.connectToBootstrapServer();
        }
        return INSTANCE;
    }
}
