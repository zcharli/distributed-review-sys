package client;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import config.APIConfig;
import config.DHTConfig;
import core.APIServer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by czl on 19/09/16.
 */
public class DRSClient {

    @Parameter
    private List<String> parameters = new ArrayList<>();

    @Parameter(names = "-port", description = "Port to host API")
    private Integer port = -1;

    @Parameter(names = "-host", description = "Host address for API")
    private String host;

    @Parameter(names = "-bootstrap", description = "Run the bootstrap version of the DHT")
    private boolean bootstrap = false;

    @Parameter(names = "-dht_interface", description = "DHT listen interface")
    private String dhtInterface;

    public void run() {
        if (bootstrap) {
            DHTConfig.instance().isBootstrap = bootstrap;
        }
        if (host == null) {
            host = APIConfig.DEFAULT_HOST;
        }
        if (port == -1) {
            port = APIConfig.API_PORT;
        } else {
            APIConfig.API_PORT = port;
        }
        if (dhtInterface != null) {
            DHTConfig.DHT_LISTEN_INTERFACE = dhtInterface;
        }
        APIServer server = new APIServer(host, port);
    }

    public static void main(String[] args) {
        DRSClient client = new DRSClient();
        new JCommander(client, args);
        client.run();
    }
}
