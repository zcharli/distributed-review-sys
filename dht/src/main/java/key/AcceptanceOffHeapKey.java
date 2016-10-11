package key;

/**
 * Possible no use
 * Created by cli on 10/1/2016.
 */
public class AcceptanceOffHeapKey extends DefaultOffHeapKey {

    protected AcceptanceOffHeapKey() {}

    public static AcceptanceOffHeapKeyBuilder builder() {
        return new AcceptanceOffHeapKeyBuilder();
    }

    public static class AcceptanceOffHeapKeyBuilder extends DefaultOffHeapKey.OffHeapKeyBuilder {
        public AcceptanceOffHeapKeyBuilder() {}

        public String buildReviewKey() {
            return String.format("%s%s%s", m_prefix, "acceptance:", m_id);
        }
    }
}
