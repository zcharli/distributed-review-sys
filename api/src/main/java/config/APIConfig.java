package config;

/**
 * Created by cli on 9/27/2016.
 */
public class APIConfig {
    public static int API_PORT = 9214;
    public static final APIConfig INSTANCE;

    private APIConfig() {}

    public static APIConfig instance() {
        return INSTANCE;
    }

    static {
        INSTANCE = new APIConfig();
    }
}
