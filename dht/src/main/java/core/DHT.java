package core;

import key.DRSKey;
import msg.AsyncComplete;
import msg.AsyncResult;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.FutureRemove;
import net.tomp2p.futures.BaseFutureListener;
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

    /**
     * Always puts new reviews into acceptance domain
     * @param key
     * @param element
     * @param callback
     * @throws IOException
     */
    public void put(KEY key, Object element, AsyncComplete callback) throws IOException {
        if (element == null || key == null) {
            return;
        }

        FuturePut futurePut = m_profile.MY_PROFILE.put( key.getLocationKey() )
                .data( key.getDomainKey(), key.getContentKey(), new Data( element ) )
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

    public void add(KEY key, Object element, AsyncComplete callback) throws IOException {
        if (key == null || element == null) {
            LOGGER.error("Invalid add request");
            return;
        }

        FuturePut futurePut = m_profile.MY_PROFILE.add( key.getLocationKey() )
                .data( new Data( element ) ).domainKey( key.getDomainKey() ).start();

        attachFutureListenerToPut(futurePut, callback);
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
     * Gets all async
     * @param key
     * @param callback
     * @return
     */
    public void get(KEY key, AsyncResult callback) {
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
    public void remove(KEY key, AsyncComplete callback) {
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
}
