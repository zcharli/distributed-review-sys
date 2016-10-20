package config;


import net.tomp2p.peers.Number160;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by czl on 19/09/16.
 */
public class DHTConfig {

    public static final InetAddress BOOTSRAP_ADDR;
    public static final String KEYSTORE_ADDR = "keystore";
    public static final String TRACKED_ID = "trk:loc:";
    public static final String REDIS_USERNAME_PREFIX = "usr:";
    public static final JedisPool REDIS_RESOURCE_POOL = new JedisPool(new JedisPoolConfig(), DHTConfig.REDIS_HOST);
    public static int DRS_PORT;
    public static final String REDIS_HOST = "localhost";
    public static int MAX_CACHE = 1000; // number of products tracked by this node
    public static final Number160 ACCEPTANCE_DOMAIN = Number160.createHash("reviews_staging_domain");
    public static final Number160 PUBLISHED_DOMAIN = Number160.createHash("reviews_published_domain");;
    public boolean isBootstrap = false;
    public boolean willPersistData = true;
    public static boolean collectMetrics = false;
    public static String DHT_LISTEN_INTERFACE = "ens3";
    public static String MY_DOMAIN = "anonymous"; // Set to distinguish this node from other nodes
    private static DHTConfig INSTANCE;
    private DHTConfig() {}

    static {
        DRS_PORT = 4000;
        InetAddress tempAddr = null;
        try {
            // TODO: code for changing IP addresses incase .19 changes
            tempAddr = Inet4Address.getByName("192.168.101.19");
        } catch (UnknownHostException e) {
            Logger.getGlobal().log(Level.SEVERE, "Host InetAddress was not found.");
        } finally {
            BOOTSRAP_ADDR = tempAddr;
        }
    }

    public static DHTConfig instance() {

        if (INSTANCE == null) {
            INSTANCE = new DHTConfig();
        }
        return INSTANCE;
    }
}
