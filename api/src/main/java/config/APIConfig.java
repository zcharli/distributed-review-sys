package config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.net.URLDecoder;

/**
 * Created by cli on 9/27/2016.
 */
public class APIConfig {
    private final static Logger LOGGER = LoggerFactory.getLogger(APIConfig.class);

    public static int API_PORT = 8080;
    public static final String DEFAULT_HOST = "127.0.0.1";
    public static final APIConfig INSTANCE;
    public static final String WEB_RESOURCE_PATH;
    public static final String DEFAULT_STEP = "5";


    static {
        INSTANCE = new APIConfig();
        String path = INSTANCE.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        try {
            String decodedPath = URLDecoder.decode(path, "UTF-8");
            LOGGER.error(decodedPath);
        } catch (Exception e) {
            e.printStackTrace();
        }

        URL resourceURL = INSTANCE.getClass().getClassLoader().getResource("webapp");
        WEB_RESOURCE_PATH = resourceURL == null ? null : resourceURL.toExternalForm();
        if (WEB_RESOURCE_PATH == null ) {
            LOGGER.error("Web Resource Path not found! Exiting!");
            System.exit(0);
        } else {
            LOGGER.debug("Set web resource path to: " + WEB_RESOURCE_PATH);
        }
        System.out.println("Set web resource path to: " + WEB_RESOURCE_PATH);
    }

    private APIConfig() {}

    public static APIConfig instance() {
        return INSTANCE;
    }
}
