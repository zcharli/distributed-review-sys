package config;


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
    public static final int DRS_PORT;

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

    public static DHTConfig getInstance() {

        if (INSTANCE == null) {
            INSTANCE = new DHTConfig();
        }
        return INSTANCE;
    }

}
