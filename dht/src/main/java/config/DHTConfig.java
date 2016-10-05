package config;


import net.tomp2p.peers.Number160;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by czl on 19/09/16.
 */
public class DHTConfig {

    public static final InetAddress BOOTSRAP_ADDR;
    public static int DRS_PORT;
    public static final String REDIS_HOST = "localhost";
    public static final int REDIS_PORT = 6379;
    public static final Number160 ACCEPTANCE_DOMAIN = Number160.createHash("reviews_staging_domain");
    public static final Number160 PUBLISHED_DOMAIN = ACCEPTANCE_DOMAIN;//Number160.createHash("published_reviews_domain");
    public boolean isBootstrap = false;
    public static boolean collectMetrics = false;
    public static String DHT_LISTEN_INTERFACE = "ens3";

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
