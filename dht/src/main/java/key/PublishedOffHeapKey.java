package key;

/**
 * This key is used to identify a published review
 * Possible no use
 * Created by cli on 10/1/2016.
 */
public class PublishedOffHeapKey extends DefaultOffHeapKey {

    public PublishedOffHeapKey() {}

    public static PublishedOffHeapKeyBuilder builder() {
        return new PublishedOffHeapKeyBuilder();
    }

    public static class PublishedOffHeapKeyBuilder extends DefaultOffHeapKey.OffHeapKeyBuilder {
        public PublishedOffHeapKeyBuilder() {}
    }
}
