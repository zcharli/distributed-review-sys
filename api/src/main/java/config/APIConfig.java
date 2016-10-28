package config;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.net.URLDecoder;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

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

    public static final ImmutableList<String> SEARCH_CATEGORIES;
    public static final Queue<String> CURRENT_SEARCH_CATEGORIES;
    public static int MAX_RESULTS_PER_CATEGORY = 5;
    public static int MAX_TOKEN_SESSIONS = 10;
    public static String STATIC_DYNAMIC_HOME = System.getProperty("user.home") + "/static";
    public static String IMAGE_RESOURCE_PATH = "/images";
    public static String IMAGE_UPLOAD_LOCATION = STATIC_DYNAMIC_HOME + IMAGE_RESOURCE_PATH;
    public static String ABSOLUTE_IMAGE_RESOURCE_PATH = DEFAULT_HOST + IMAGE_UPLOAD_LOCATION;


    public static final ImmutableSet<String> LIVE_PRODUCT_TYPES = ImmutableSet.of("commodity");

    static {
        SEARCH_CATEGORIES = ImmutableList.of("Type", "Review", "Product", "Product Identifier");
        CURRENT_SEARCH_CATEGORIES = new ConcurrentLinkedQueue<String>();
        CURRENT_SEARCH_CATEGORIES.addAll(SEARCH_CATEGORIES);
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
