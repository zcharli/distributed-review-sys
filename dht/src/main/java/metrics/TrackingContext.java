package metrics;

import config.DHTConfig;

/**
 * Created by cli on 10/10/2016.
 */
public class TrackingContext {
    public int[] locationBuffer;
    public long numberOfHits;
    public String trackedDomain;

    public TrackingContext() { }

    public TrackingContext(int[] locationBuf) {
        locationBuffer = locationBuf;
        numberOfHits = 1;
        trackedDomain = DHTConfig.MY_DOMAIN;
    }
}
