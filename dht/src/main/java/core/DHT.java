package core;

import config.DHTConfig;
import key.DRSKey;
import msg.AsyncComplete;
import msg.AsyncResult;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.futures.BaseFutureListener;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.storage.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * Created by cli on 9/20/2016.
 */
public class DHT<KEY extends DRSKey> {

    DHTProfile m_profile;

    private final static Logger LOGGER = LoggerFactory.getLogger(DHT.class);

    public DHT() {
        m_profile = DHTProfile.instance();
        if (m_profile == null) {
            LOGGER.error("Failed to initialize DHT, likely tried to get instance DHT before init DHTProfile");
        }
    }

    public void put(KEY key, Object element, AsyncComplete callback) throws IOException {
        if (element == null || key == null) {
            return;
        }

        FuturePut futurePut = m_profile.MY_PROFILE.put( Number160.createHash( key.getLocationKey() ) )
                .data( new Number160( key.getContentKey() ), new Data( element ) )
                .domainKey(  DHTConfig.instance().domainKey() )
                .start();

        attachFutureListenerToPut(futurePut, callback);
    }

    private void attachFutureListenerToPut(FuturePut futurePut, AsyncComplete callback) {
        futurePut.addListener(new BaseFutureListener<FuturePut>() {
            @Override
            public void operationComplete(FuturePut future) throws Exception {
                if (future == null || future.isFailed()) {
                    LOGGER.warn("Future object failed to return from put(KEY key, Object element, AsyncResult callback) or is null.");
                }
                callback.isSuccessful(future.isSuccess());
                callback.call();
            }

            @Override
            public void exceptionCaught(Throwable t) throws Exception {
                LOGGER.warn(String.format("Failed to put %s to dht: " + t.getMessage()));
                callback.isSuccessful(false);
                callback.call();
            }
        });
    }

    /**
     * The first step in the workflow to publish a review
     * @param key
     * @param element
     * @param callback
     * @throws IOException
     */
    public void addAcceptance(KEY key, Object element, AsyncComplete callback) throws IOException {
        if (element == null || key == null) {
            return;
        }

        FuturePut futurePut = m_profile.MY_PROFILE.put(Number160.createHash(key.getLocationKey()))
                .data(new Number160(key.getContentKey()), new Data(element))
                .domainKey(DHTConfig.ACCEPTANCE_DOMAIN)
                .start();
        attachFutureListenerToPut(futurePut, callback);
    }

    public void add(KEY key, Object element, AsyncComplete callback) throws IOException {
        if (key == null || element == null) {
            return;
        }

        FuturePut futurePut = m_profile.MY_PROFILE.add( Number160.createHash( key.getLocationKey() ) )
                .data( new Data( element ) ).domainKey( DHTConfig.instance().domainKey() ).start();

        futurePut.addListener(new BaseFutureListener<FuturePut>() {
            @Override
            public void operationComplete(FuturePut future) throws Exception {
                if (future == null || future.isFailed()) {
                    LOGGER.warn("Future object failed to return from add(KEY key, Object element, AsyncResult callback) or is null.");
                }

                callback.isSuccessful(future.isSuccess());
                callback.call();
            }

            @Override
            public void exceptionCaught(Throwable t) throws Exception {
                LOGGER.warn(String.format("Failed to add %s to dht: " + t.getMessage()));
                callback.isSuccessful(false);
                callback.call();
            }
        });
    }

    /**
     * Gets all sync
     * @param key
     * @return
     */
    @Nullable
    public Collection<Data> get(KEY key) {
        if (key == null) {
            return null;
        }

        FutureGet futureGet = m_profile.MY_PROFILE.get( Number160.createHash( key.getLocationKey() ) )
                .all().domainKey( DHTConfig.instance().domainKey() )
                .start();

        futureGet.awaitUninterruptibly(2000);
        Object ret = null;

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
     * Gets all async
     * @param key
     * @param callback
     * @return
     */
    public void get(KEY key, AsyncResult callback) {
        if (key == null) {
            return;
        }
        FutureGet futureGet = m_profile.MY_PROFILE.get( Number160.createHash( key.getLocationKey() ) )
                .all().domainKey( DHTConfig.instance().domainKey() )
                .start();

        futureGet.addListener(new BaseFutureListener<FutureGet>() {
            @Override
            public void operationComplete(FutureGet future) throws Exception {
                if (future == null || future.isFailed()) {
                    //LOGGER.log(Level.WARNING, "Future object failed to return from get(KEY key, AsyncResult callback) or is null.");
                    callback.call();
                    return;
                }

                Object ret = null;
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
    public void delete(KEY key, AsyncComplete callback) {
        if (key == null) {
            return;
        }

    }
}
