package core;

import config.DHTConfig;
import exceptions.InitializationFailedException;
import net.tomp2p.connection.Bindings;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDiscover;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by czl on 20/09/16.
 */
public class DHTProfile {

    private static DHTProfile INSTANCE;

    private final static Logger LOGGER = Logger.getLogger(DHTProfile.class.getName());

    public final PeerDHT MY_PROFILE;

    private DHTProfile(boolean isBootStrap) throws InitializationFailedException {
        Random r = new Random(42L);
        PeerDHT currentClient = null;
        try {
            if (isBootStrap) {
                Bindings b = new Bindings();
                b.addAddress(DHTConfig.BOOTSRAP_ADDR);
                currentClient = new PeerBuilderDHT(new PeerBuilder(new Number160(r)).bindings(b).ports(DHTConfig.DRS_PORT).start()).start();
            } else {
                System.out.println("Trying to connect to: " + DHTConfig.BOOTSRAP_ADDR.getHostAddress() + ":" + DHTConfig.DRS_PORT);
                currentClient = new PeerBuilderDHT(new PeerBuilder(new Number160(r)).ports(DHTConfig.DRS_PORT).behindFirewall().start()).start();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "DHTProfile failed to activate peer.");
            e.printStackTrace();
        } finally {
            MY_PROFILE = currentClient;
        }
    }

    public static DHTProfile instance()  {
        if (INSTANCE == null) {
            LOGGER.log(Level.WARNING, "DHTProfile.init(isBootstrap) must be called first");
            return null;
        }
        return INSTANCE;
    }

    private void connectToBootstrapServer() throws InitializationFailedException {
        PeerAddress bootStrapServer = new PeerAddress(Number160.ZERO, DHTConfig.BOOTSRAP_ADDR, DHTConfig.DRS_PORT,DHTConfig.DRS_PORT,DHTConfig.DRS_PORT+1);
        FutureDiscover fd = MY_PROFILE.peer().discover().peerAddress(bootStrapServer).start();
        fd.awaitUninterruptibly();

        if (!fd.isSuccess()) {
            LOGGER.log(Level.SEVERE, "Unable to find profile's outside address");
            throw new InitializationFailedException("DHTProfile failed it's discovery phase: " + fd.failedReason());
        } else {
            LOGGER.log(Level.INFO, "Client successfully discovered bootstrap server.");
        }

        bootStrapServer = fd.reporter();
        FutureBootstrap bootstrap = MY_PROFILE.peer().bootstrap().peerAddress(bootStrapServer).start();
        bootstrap.awaitUninterruptibly();
        if (!bootstrap.isSuccess()) {
            LOGGER.log(Level.SEVERE, "Unable to find bootstrap this client.");
            throw new InitializationFailedException("DHTProfile failed it's bootstrap phase: " + bootstrap.failedReason());
        } else {
            LOGGER.log(Level.INFO, "Client successfully connected to bootstrap server.");
        }
    }

    /**
     * Should only be called once
     * @param isBootstrap
     * @return
     * @throws InitializationFailedException
     */
    public static DHTProfile init(boolean isBootstrap) throws InitializationFailedException {
        if (INSTANCE != null) {
            throw new InitializationFailedException("DHTProfile instance has already been initialized");
        }

        INSTANCE = new DHTProfile(isBootstrap);
        if (!isBootstrap) {
            INSTANCE.connectToBootstrapServer();
        }
        return INSTANCE;
    }
}
