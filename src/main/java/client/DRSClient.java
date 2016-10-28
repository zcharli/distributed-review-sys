package client;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.common.base.Strings;
import config.APIConfig;
import config.DHTConfig;
import core.APIServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by czl on 19/09/16.
 */
public class DRSClient {
    private final static Logger LOGGER = LoggerFactory.getLogger(DRSClient.class);

    @Parameter
    private List<String> parameters = new ArrayList<>();

    @Parameter(names = "-drsport", description = "DRS port for DHT")
    private Integer drsport = -1;

    @Parameter(names = "-port", description = "Port to host API")
    private Integer port = -1;

    @Parameter(names = "-host", description = "Host address for API")
    private String host;

    @Parameter(names = "-custombootstrap", description = "Used for client nodes to connect to the custom bootstrap address,")
    private String customBootstrap;

    @Parameter(names = "-bootstrap", description = "Run the bootstrap version of the DHT")
    private String bootstrap;

    @Parameter(names = "-persistance", description = "Run the bootstrap version of the DHT")
    private boolean persistance = true;

    @Parameter(names = "-dht_interface", description = "DHT listen interface")
    private String dhtInterface;

    public void run() {

        DHTConfig.instance().isBootstrap = !Strings.isNullOrEmpty(bootstrap);
        if (DHTConfig.instance().isBootstrap) {
            try {
                DHTConfig.instance().setBootstrapAddress(bootstrap);
            } catch (Exception e) {
                LOGGER.error("Bootstrap address initialization error.");
                e.printStackTrace();
                System.exit(0);
            }
        }

        if (!Strings.isNullOrEmpty(customBootstrap)) {
            try {
                DHTConfig.instance().setBootstrapAddress(customBootstrap);
            } catch (Exception e) {
                LOGGER.error("Could not resolve custom bootstrap address.");
                System.exit(0);
            }
        }

        DHTConfig.instance().willPersistData = persistance;

        if (drsport != -1) {
            DHTConfig.instance().setDRSPort(drsport);
        }

        if (host == null) {
            host = APIConfig.DEFAULT_HOST;
        } else {
            APIConfig.setDynamicResourcePath(host);
        }
        if (port == -1) {
            port = APIConfig.API_PORT;
        } else {
            APIConfig.API_PORT = port;
        }
        if (dhtInterface != null) {
            DHTConfig.DHT_LISTEN_INTERFACE = dhtInterface;
        }

        try {
            APIServer server = new APIServer(host, port);
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        DRSClient client = new DRSClient();
        new JCommander(client, args);
        client.run();
    }
}
