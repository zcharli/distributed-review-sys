package key;

import com.google.common.base.Strings;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by cli on 9/23/2016.
 */
public class DefaultDHTKeyPair implements DRSKey {
    private final static Logger LOGGER = Logger.getLogger(DefaultDHTKeyPair.class.getName());

    private static final String DEFAULT_LOC = "defaultLocation";
    private static final String DEFAULT_CON = "defaultContent";

    private String m_locationKey;
    private String m_contentKey;

    public static class DefaultDHTKeyBuilder extends DHTKeyBuilder {
        private String m_locationKey;
        private String m_contentKey;

        public DefaultDHTKeyBuilder() {}

        public DefaultDHTKeyBuilder locationKey(String loc) {
            if (loc.length() >= MAX_KEY_LENGTH) {
                loc = loc.substring(0, MAX_KEY_LENGTH - 1);
            }
            m_locationKey = loc;
            return this;
        }

        public DefaultDHTKeyBuilder contentKey(String con) {
            if (con.length() >= MAX_KEY_LENGTH) {
                con = con.substring(0, MAX_KEY_LENGTH - 1);
            }
            m_contentKey = con;
            return this;
        }

        public DRSKey build() {
            return new DefaultDHTKeyPair(m_locationKey, m_contentKey);
        }
    }

    public DefaultDHTKeyPair(String locationKey, String contentKey) {
        m_locationKey = locationKey;
        m_contentKey = contentKey;
    }

    public String getLocationKey() {
        if (Strings.isNullOrEmpty(m_locationKey)) {
            LOGGER.log(Level.WARNING, "DRS key found null location key, return default");
            return DEFAULT_LOC;
        }
        return m_locationKey;
    }

    public String getContentKey() {
        if (Strings.isNullOrEmpty(m_contentKey)) {
            LOGGER.log(Level.WARNING, "DRS key found null contentKey key, return default");
            return DEFAULT_CON;
        }
        return m_contentKey;
    }

    public static DefaultDHTKeyBuilder builder() {
        return new DefaultDHTKeyBuilder();
    }
}
