package core;

import com.fasterxml.jackson.databind.ObjectMapper;
import config.DHTConfig;
import key.DRSKey;
import key.DefaultDHTKeyPair;
import msg.AsyncComplete;
import msg.AsyncResult;
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
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

/**
 * Created by cli on 9/20/2016.
 */
public class DHT<KEY extends DRSKey> {

    private final DHTProfile m_profile;
    private final ExecutorService m_keystoreWorker;
    private final ObjectMapper mapper;

    private final static Logger LOGGER = LoggerFactory.getLogger(DHT.class);

    public DHT() {
        m_profile = DHTProfile.instance();
        m_keystoreWorker = Executors.newSingleThreadExecutor();
        mapper = new ObjectMapper();
        if (m_profile == null) {
            LOGGER.error("Failed to initialize DHT, likely tried to get instance DHT before init DHTProfile");
        }
    }

    /**
     * Always puts new reviews into acceptance domain
     * @param key
     * @param element
     * @param callback
     * @throws IOException
     */
    public void put(final KEY key, final Object element, final AsyncComplete callback) throws IOException {
        if (element == null || key == null) {
            return;
        }

        FuturePut futurePut = m_profile.MY_PROFILE.put( key.getLocationKey() )
                .data( key.getDomainKey(), key.getContentKey(), new Data( element ) )
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

        FuturePut futurePut = m_profile.MY_PROFILE.add( key.getLocationKey() )
                .data( new Data( element ) ).domainKey( key.getDomainKey() ).start();

        attachFutureListenerToPut(futurePut, callback, key);
    }

    public Boolean recordMetric() {
        return true;
    }

    /**
     * Gets all sync
     * @param key
     * @return
     */
    @Nullable
    public Collection<Data> get(final KEY key) {
        if (key == null) {
            return null;
        }

        FutureGet futureGet = m_profile.MY_PROFILE.get( key.getLocationKey() )
                .all().domainKey( key.getDomainKey() )
                .start();

        futureGet.awaitUninterruptibly(2000);

        if (futureGet.isSuccess() && futureGet.data() != null) {
            try {
                Map<Number640, Data> dataMap = futureGet.dataMap();
                if (dataMap.size() != 0) {
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
     * @param key
     * @param callback
     * @return
     */
    public void get(final KEY key, final AsyncResult callback) {
        if (key == null) {
            return;
        }

        FutureGet futureGet = m_profile.MY_PROFILE.get( key.getLocationKey() )
                .all()
                .domainKey( key.getDomainKey() )
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
                    callback.payload( dataMap );
                }
                callback.call();
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
     * @param key
     * @param callback
     */
    public void remove(final KEY key, final AsyncComplete callback) {
        if (key == null) {
            return;
        }
        FutureRemove futureRemove = m_profile.MY_PROFILE
                .remove( key.getLocationKey() )
                .contentKey( key.getContentKey() )
                .domainKey( key.getDomainKey() )
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

    public void addToKeyStore(final Number160 locationKey, Function<Boolean,Boolean> callback) {
        final CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
            final int[] locationKeyBuffer = locationKey.toIntArray();
            try (Jedis adapter = DHTConfig.REDIS_RESOURCE_POOL.getResource()) {
                String json = "";
                try {
                    json = mapper.writeValueAsString(locationKeyBuffer);
                } catch(Exception e) {
                    LOGGER.error("JSON processing exception on addToKeyStore: "+ e.getMessage());
                    return false;
                }
                adapter.hset(DHTConfig.KEYSTORE_ADDR, json, json);
            } catch (Exception e) {
                LOGGER.error("Jedis resource fetch error on addToKeyStore: " + e.getMessage());
                return false;
            }
            return true;
        }, m_keystoreWorker)
                .thenApply(callback)
                .exceptionally(ex -> {
                    LOGGER.error("An exception occured asynchronously when addToKeyStore: " + ex.getMessage());
                    return false;
                });
    }

    /**
     * Synchronous
     * @return
     */
    public List<Number160> getKeyStore() {
        final List<Number160> list = new LinkedList<>();
        try (Jedis adapter = DHTConfig.REDIS_RESOURCE_POOL.getResource()) {
            List<String> serializedIntArrays = adapter.lrange(DHTConfig.KEYSTORE_ADDR, 0, -1);
            for (String number160Buffer : serializedIntArrays) {
                try {
                    int[] buffer = mapper.readValue(number160Buffer, int[].class);
                    Number160 locationKey = new Number160(buffer);
                    list.add(locationKey);
                } catch (Exception e) {
                    LOGGER.error("An error occurec when trying to deserialize number160 buffer: " + e.getMessage());
                }
            }
        }
        return list;
    }
}
