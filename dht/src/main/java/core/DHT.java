package core;

import com.fasterxml.jackson.databind.ObjectMapper;
import config.DHTConfig;
import key.DRSKey;
import key.DefaultOffHeapKey;
import msg.AsyncComplete;
import msg.AsyncResult;
import msg.RedisElementContainer;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.FutureRemove;
import net.tomp2p.futures.BaseFutureListener;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.storage.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;

/**
 * Created by cli on 9/20/2016.
 */
public class DHT<KEY extends DRSKey> {

    private final DHTProfile m_profile;
    private final ExecutorService m_persistentWorker;
    private final ObjectMapper m_mapper;
    private final ConcurrentMap<Number160, Integer> m_keystoreCache;

    private final static Logger LOGGER = LoggerFactory.getLogger(DHT.class);

    public DHT() {
        m_profile = DHTProfile.instance();
        m_persistentWorker = Executors.newSingleThreadExecutor();
        m_mapper = new ObjectMapper();
        m_keystoreCache = new ConcurrentHashMap<>();
        // Initializes keyStore cache
        getKeyStore();
        if (m_profile == null) {
            LOGGER.error("Failed to initialize DHT, likely tried to get instance DHT before init DHTProfile");
        }
    }

    /**
     * Always puts new reviews into acceptance domain
     *
     * @param key
     * @param element
     * @param callback
     * @throws IOException
     */
    public void put(final KEY key, final Object element, final AsyncComplete callback) throws IOException {
        if (element == null || key == null) {
            return;
        }

        FuturePut futurePut = m_profile.MY_PROFILE.put(key.getLocationKey())
                .data(key.getDomainKey(), key.getContentKey(), new Data(element))
                .start();

        attachFutureListenerToPut(futurePut, callback, key);
    }

    private void attachFutureListenerToPut(final FuturePut futurePut, final AsyncComplete callback, final KEY key) {
        DHT self = this;
        futurePut.addListener(new BaseFutureListener<FuturePut>() {
            @Override
            public void operationComplete(FuturePut future) throws Exception {
                if (future == null || future.isFailed()) {
                    LOGGER.warn("Future object failed to return from put(KEY key, Object element, AsyncResult callback) or is null.");
                }
                callback.isSuccessful(future.isSuccess());
                callback.call();
                addToKeyStore(key.getLocationKey(), r -> self.recordMetric());
            }

            @Override
            public void exceptionCaught(Throwable t) throws Exception {
                LOGGER.warn(String.format("Failed to put %s to dht: " + t.getMessage()));
                callback.isSuccessful(false);
                callback.call();
            }
        });
    }

    public void add(final KEY key, final Object element, final AsyncComplete callback) throws IOException {
        if (key == null || element == null) {
            LOGGER.error("Invalid add request");
            return;
        }

        FuturePut futurePut = m_profile.MY_PROFILE.add(key.getLocationKey())
                .data(new Data(element)).domainKey(key.getDomainKey()).start();

        attachFutureListenerToPut(futurePut, callback, key);
    }

    public Boolean recordMetric() {
        return true;
    }

    /**
     * Gets all sync
     *
     * @param key
     * @return
     */
    @Nullable
    public Collection<Data> get(final KEY key) {
        if (key == null) {
            return null;
        }

        FutureGet futureGet = m_profile.MY_PROFILE.get(key.getLocationKey())
                .all().domainKey(key.getDomainKey())
                .start();

        futureGet.awaitUninterruptibly(2000);

        if (futureGet.isSuccess() && futureGet.data() != null) {
            try {
                Map<Number640, Data> dataMap = futureGet.dataMap();
                if (dataMap.size() != 0) {
                    addToKeyStore(key.getLocationKey(), r -> recordMetric());
                    return dataMap.values();
                }
            } catch (Exception e) {
                LOGGER.warn("Exception while decoding futureGet: " + e.getMessage());
            }
        }
        return null;
    }

    /**
     * Gets all async for one product
     *
     * @param key
     * @param callback
     * @return
     */
    public void get(final KEY key, final AsyncResult callback) {
        if (key == null) {
            return;
        }

        FutureGet futureGet = m_profile.MY_PROFILE.get(key.getLocationKey())
                .all()
                .domainKey(key.getDomainKey())
                .start();

        futureGet.addListener(new BaseFutureListener<FutureGet>() {
            @Override
            public void operationComplete(FutureGet future) throws Exception {
                if (future == null || future.isFailed()) {
                    //LOGGER.log(Level.WARNING, "Future object failed to return from get(KEY key, AsyncResult callback) or is null.");
                    callback.call();
                    return;
                }
                callback.isSuccessful(futureGet.isSuccess());
                Map<Number640, Data> dataMap = future.dataMap();
                if (dataMap.size() != 0) {
                    callback.payload(dataMap);
                }
                callback.call();
                addToKeyStore(key.getLocationKey(), r -> recordMetric());
            }

            @Override
            public void exceptionCaught(Throwable t) throws Exception {
                LOGGER.warn(String.format("Failed to get %s from dht: " + t != null && t.getMessage() != null ? t.getMessage() : "unknown"));
                callback.call();
            }
        });
        return;
    }

    /**
     * Generally used for deleting staging content
     *
     * @param key
     * @param callback
     */
    public void remove(final KEY key, final AsyncComplete callback) {
        if (key == null) {
            return;
        }
        FutureRemove futureRemove = m_profile.MY_PROFILE
                .remove(key.getLocationKey())
                .contentKey(key.getContentKey())
                .domainKey(key.getDomainKey())
                .start();

        futureRemove.addListener(new BaseFutureListener<FutureRemove>() {
            @Override
            public void operationComplete(FutureRemove future) throws Exception {
                if (future == null || future.isFailed()) {
                    LOGGER.warn("Future object failed to return from remove(KEY key, AsyncComplete callback) or is null.");
                    callback.call();
                    return;
                }

                callback.isSuccessful(future.isSuccess() && future.isRemoved());
                callback.call();
            }

            @Override
            public void exceptionCaught(Throwable t) throws Exception {
                LOGGER.warn(String.format("Failed to remove %s from dht: " + t != null && t.getMessage() != null ? t.getMessage() : "unknown"));
                callback.call();
            }
        });
    }

    public void removeDataFromStaging(Map.Entry<Number640, Data> entryToRemove) {
        final CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
            try (Jedis adapter = DHTConfig.REDIS_RESOURCE_POOL.getResource()) {
                final Number640 key = entryToRemove.getKey();
                try {
                    String dataJson = m_mapper.writeValueAsString(RedisElementContainer.builder()
                            .setBuffer(entryToRemove.getValue().toBytes())
                            .setContentBuffer(key.contentKey().toIntArray())
                            .setLocationBuffer(key.locationKey().toIntArray())
                            .setVersionBuffer(key.versionKey().toIntArray())
                            .setDomainBuffer(key.domainKey().toIntArray())
                            .build());
                    String offHeapKey = DefaultOffHeapKey.buildOffHeapKey(key);
                    // Set 0 since we want to remove ALL acceptance objects that are equal to the current key being accepted
                    Long result = adapter.lrem(offHeapKey, 0, dataJson);
                    if (result.longValue() <= 0) {
                        LOGGER.error("Removing data from storage returned a value that was not found: " + result.longValue());
                    } else if (result.longValue() == 1) {
                        LOGGER.info("Successfully removed an acceptance record from backend: " + result.longValue());
                    } else {
                        LOGGER.error("Weird error when removing an acceptance record from backend: " + result.longValue());
                    }
                } catch (Exception e) {
                    LOGGER.error("An error occurred while serializing data to remove from acceptance set.");
                    return false;
                }
            }
            return true;
        }, m_persistentWorker);
    }

    public void addToKeyStore(final Number160 locationKey, Function<Boolean, Boolean> callback) {
        if (m_keystoreCache.containsKey(locationKey)) {
            return;
        }
        final CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
            final int[] locationKeyBuffer = locationKey.toIntArray();
            try (Jedis adapter = DHTConfig.REDIS_RESOURCE_POOL.getResource()) {
                String json = "";
                try {
                    json = m_mapper.writeValueAsString(locationKeyBuffer);
                } catch (Exception e) {
                    LOGGER.error("JSON processing exception on addToKeyStore: " + e.getMessage());
                    return false;
                }
                adapter.hset(DHTConfig.KEYSTORE_ADDR, json, json);
                m_keystoreCache.put(locationKey, 1);
            } catch (Exception e) {
                LOGGER.error("Jedis resource fetch error on addToKeyStore: " + e.getMessage());
                return false;
            }
            return true;
        }, m_persistentWorker)
                .thenApply(callback)
                .exceptionally(ex -> {
                    LOGGER.error("An exception occured asynchronously when addToKeyStore: " + ex.getMessage());
                    return false;
                });
    }

    /**
     * Synchronous
     *
     * @return
     */
    public Collection<Number160> getKeyStore() {
        if (m_keystoreCache.size() > 0) {
            // keystore cache is initialized, return it.
            Collection<Number160> keyCollection = m_keystoreCache.keySet();
            return keyCollection;
        }
        final Collection<Number160> list = new LinkedList<>();
        try (Jedis adapter = DHTConfig.REDIS_RESOURCE_POOL.getResource()) {
            Set<String> serializedIntArrays = adapter.hkeys(DHTConfig.KEYSTORE_ADDR);
            for (String number160Buffer : serializedIntArrays) {
                try {
                    int[] buffer = m_mapper.readValue(number160Buffer, int[].class);
                    Number160 locationKey = new Number160(buffer);
                    list.add(locationKey);
                    m_keystoreCache.put(locationKey, 1);
                } catch (Exception e) {
                    LOGGER.error("An error occured when trying to deserialize Number160 buffer: " + e.getMessage());
                }
            }
        }
        return list;
    }
}
