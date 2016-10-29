package metrics;

import config.DHTConfig;
import net.tomp2p.peers.Number160;

/**
 * Created by cli on 10/10/2016.
 */
public class TrackingContext implements TrackingContextView {
    public Number160 locationKey;
    public long numberOfHits;
    public String trackedDomain;
    public MetricsCollector.TrackingType trackingType;
    public long lastModified;

    public TrackingContext() { }

    public TrackingContext(Number160 locationBuf, MetricsCollector.TrackingType type) {
        locationKey = locationBuf;
        numberOfHits = 1;
        trackedDomain = DHTConfig.MY_DOMAIN;
        trackingType = type;
    }
    public Number160 getPrimary() {
        return locationKey;
    }

    public MetricsCollector.TrackingType getType() {
        return trackingType;
    }

    public synchronized void incrementHits() {
        numberOfHits++;
        lastModified = System.currentTimeMillis();
    }

    public synchronized long getValue() {
        return numberOfHits;
    }
}
