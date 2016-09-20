package core;

import com.sun.istack.internal.Nullable;
import config.DHTConfig;
import exceptions.InitializationFailedException;
import key.DRSKey;
import msg.AsyncComplete;
import msg.AsyncResult;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.futures.BaseFutureListener;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.storage.Data;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by cli on 9/20/2016.
 */
public class DHT<KEY extends DRSKey> {

    DHTProfile m_profile;

    private final static Logger LOGGER = Logger.getLogger(DHT.class.getName());

    public DHT() {
        try {
            m_profile = DHTProfile.instance();
        } catch (InitializationFailedException e) {
            e.printStackTrace();
            LOGGER.log(Level.SEVERE, "Failed to initialize DHT: " + e.getMessage());
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

        futurePut.addListener(new BaseFutureListener<FuturePut>() {
            @Override
            public void operationComplete(FuturePut future) throws Exception {
                if (future == null || future.isFailed()) {
                    LOGGER.log(Level.WARNING, "Future object failed to return from put(KEY key, Object element, AsyncResult callback) or is null.");
                }
                callback.isSuccessful(future.isSuccess());
                callback.call();
            }

            @Override
            public void exceptionCaught(Throwable t) throws Exception {
                LOGGER.log(Level.WARNING, String.format("Failed to put %s to dht: " + t.getMessage()));
                callback.isSuccessful(false);
                callback.call();
            }
        });
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
                    LOGGER.log(Level.WARNING, "Future object failed to return from add(KEY key, Object element, AsyncResult callback) or is null.");
                }

                callback.isSuccessful(future.isSuccess());
                callback.call();
            }

            @Override
            public void exceptionCaught(Throwable t) throws Exception {
                LOGGER.log(Level.WARNING, String.format("Failed to add %s to dht: " + t.getMessage()));
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
                LOGGER.log(Level.WARNING, "Exception while decoding futureGet: " + e.getMessage());
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
                    LOGGER.log(Level.WARNING, "Future object failed to return from get(KEY key, AsyncResult callback) or is null.");
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
                LOGGER.log(Level.WARNING, String.format("Failed to get %s from dht: " + t.getMessage()));
                callback.call();
            }
        });
        return;
    }
}
