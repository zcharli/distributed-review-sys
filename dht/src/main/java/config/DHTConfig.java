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

    private String domainKey = "default";
    private Number160 domainHash;

    private static DHTConfig INSTANCE;
    private DHTConfig() {}

    static {
        DRS_PORT = 4000;
        InetAddress tempAddr = null;
        try {
            tempAddr = Inet4Address.getByName("192.168.101.12");
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

    public DHTConfig domainKey(String key) {
        domainKey = key;
        domainHash = Number160.createHash(domainKey);
        return this;
    }

    public DHTConfig generateRandomDomainKey() {
        Random r = new Random(42L);
        domainKey = Integer.toString(r.nextInt());
        return this;
    }

    public Number160 domainKey() {
        if (domainHash == null) {
            domainHash = Number160.createHash(domainKey);
        }
        return domainHash;
    }
}
