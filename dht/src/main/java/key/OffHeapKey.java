package key;

import net.tomp2p.peers.Number640;

/**
 * Created by czl on 26/09/16.
 */
public class OffHeapKey {
    private static final String m_prefix = "drs:";
    private OffHeapKey() {}

    public static OffHeapKeyBuilder builder() {
        return new OffHeapKeyBuilder();
    }

    public static class OffHeapKeyBuilder {
        private static final String m_review_prefix = "review:";
        private String m_id = "";
        private OffHeapKeyBuilder() {}

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
