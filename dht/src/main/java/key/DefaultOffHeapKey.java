package key;

import config.DHTConfig;
import net.tomp2p.peers.Number640;

/**
 * Created by czl on 26/09/16.
 */
public class DefaultOffHeapKey {
    protected static final String m_prefix = "drs:";
    protected DefaultOffHeapKey() {}

    public static OffHeapKeyBuilder builder() {
        return new OffHeapKeyBuilder();
    }

    public static String buildOffHeapKey(Number640 key) {
        String offHeapKey;
        if (key.domainKey().equals(DHTConfig.ACCEPTANCE_DOMAIN)) {
            offHeapKey = AcceptanceOffHeapKey.builder().id(key).buildReviewKey();
        } else {
            offHeapKey = PublishedOffHeapKey.builder().id(key).buildReviewKey();
        }
        return offHeapKey;
    }

    public static class OffHeapKeyBuilder {
        protected static final String m_review_prefix = "review:";
        protected String m_id = "";
        public OffHeapKeyBuilder() {}

        public OffHeapKeyBuilder id(String id) {
            m_id = id;
            return  this;
        }

        public OffHeapKeyBuilder id(Number640 id) {
            m_id = id.locationKey().toString();
            return  this;
        }

        public String buildReviewKey() {
            return String.format("%s%s%s", m_prefix, m_review_prefix, m_id);
        }
    }
}
