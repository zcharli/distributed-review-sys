package key;

/**
 * Possible no use
 * Created by cli on 10/1/2016.
 */
public class AcceptanceOffHeapKey extends DefaultOffHeapKey {
    protected static final String m_prefix = "acceptance:";

    protected AcceptanceOffHeapKey() {}

    public static AcceptanceOffHeapKeyBuilder builder() {
        return new AcceptanceOffHeapKeyBuilder();
    }

    public static class AcceptanceOffHeapKeyBuilder extends DefaultOffHeapKey.OffHeapKeyBuilder {
        public AcceptanceOffHeapKeyBuilder() {}
    }
}
