package key;

import config.DHTConfig;
import net.tomp2p.peers.Number160;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by cli on 9/23/2016.
 */
public class DefaultDHTKeyPair implements DRSKey {
    private final static Logger LOGGER = Logger.getLogger(DefaultDHTKeyPair.class.getName());

    private static final String DEFAULT_LOC = "defaultLocation";

    private Number160 m_locationKey;
    private Number160 m_contentKey;
    private Number160 m_domainKey;

    public static class DefaultDHTKeyBuilder extends DHTKeyBuilder {
        private Number160 m_locationKey;
        private Number160 m_contentKey;
        private Number160 m_domainKey;

        public DefaultDHTKeyBuilder() {}

        public DefaultDHTKeyBuilder locationKey(Number160 loc) {
            m_locationKey = loc;
            return this;
        }

        public DefaultDHTKeyBuilder contentKey(Number160 con) {
            m_contentKey = con;
            return this;
        }

        public DefaultDHTKeyBuilder domainKey(Number160 domain) {
            if (domain == null) {
                return this;
            }
            m_domainKey = domain;
            return this;
        }

        public DRSKey build() {
            return new DefaultDHTKeyPair(m_locationKey, m_contentKey, m_domainKey);
        }
    }

    public DefaultDHTKeyPair(Number160 locationKey, Number160 contentKey, Number160 domainKey) {
        m_locationKey = locationKey;
        m_contentKey = contentKey;
        m_domainKey = domainKey == null ? DHTConfig.ACCEPTANCE_DOMAIN : domainKey;
    }

    public Number160 getLocationKey() {
        if (m_locationKey == null) {
            LOGGER.log(Level.WARNING, "DRS key found null location key, return default");
            return Number160.createHash(DEFAULT_LOC);
        }
        return m_locationKey;
    }

    public Number160 getContentKey() {
        if (m_contentKey == null) {
            LOGGER.log(Level.WARNING, "DRS key found null contentKey key, return default");
            return null;
        }
        return m_contentKey;
    }

    public Number160 getDomainKey() {
        return m_domainKey;
    }

    public static DefaultDHTKeyBuilder builder() {
        return new DefaultDHTKeyBuilder();
    }
}
