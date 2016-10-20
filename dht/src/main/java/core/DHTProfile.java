package core;

import com.fasterxml.jackson.databind.ObjectMapper;
import config.DHTConfig;
import exceptions.InitializationFailedException;
import metrics.ConcurrentTrackingList;
import metrics.TrackingContext;
import net.tomp2p.connection.Bindings;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDiscover;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.util.Random;

/**
 * Created by czl on 20/09/16.
 */
public class DHTProfile {

    private static DHTProfile INSTANCE;

    private final static Logger LOGGER = LoggerFactory.getLogger(DHTProfile.class);

    public final PeerDHT MY_PROFILE;

    private DHTProfile(boolean isBootStrap, boolean isPersistent) throws InitializationFailedException {
        PeerDHT currentClient = null;
        try {
            // OffHeapStorage can be configured for versions and version check intervals
            OffHeapStorage storageLayer = isPersistent ? new OffHeapStorage().loadFromDisk() : null;
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
        }
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

    public ConcurrentTrackingList<Number160, TrackingContext> loadTrackedData() {
        ConcurrentTrackingList<Number160, TrackingContext> cache = new ConcurrentTrackingList<Number160, TrackingContext>();
        final ObjectMapper objectMapper = new ObjectMapper();
        try (Jedis adapter = DHTConfig.REDIS_RESOURCE_POOL.getResource()) {
            for (String jsonData : adapter.lrange(DHTConfig.TRACKED_ID, 0, -1)) {
                try {
                    TrackingContext fromBuffer = objectMapper.readValue(jsonData, TrackingContext.class);
                    Number160 keyFromBuffer = new Number160(fromBuffer.locationBuffer);
                    cache.silentLoad(keyFromBuffer, fromBuffer);
                } catch (Exception e) {
                    LOGGER.error("Error reading in load tracked data");
                }
            }
        }
        return cache;
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
