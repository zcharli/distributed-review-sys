package metrics;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import config.DHTConfig;
import net.tomp2p.peers.Number160;

/**
 * Created by cli on 10/10/2016.
 */
public class TrackingContext implements TrackingContextView {
    public int[] locationKey;
    public long numberOfHits;
    public String trackedDomain;
    public MetricsCollector.TrackingType trackingType;
    public long lastModified;

    public TrackingContext() { }

    public TrackingContext(Number160 locationBuf, MetricsCollector.TrackingType type) {
        locationKey = locationBuf.toIntArray();
        numberOfHits = 1;
        trackedDomain = DHTConfig.MY_DOMAIN;
        trackingType = type;
    }

    @JsonIgnore
    public Number160 getPrimary() {
        return new Number160(locationKey);
    }

    @JsonIgnore
    public MetricsCollector.TrackingType getType() {
        return trackingType;
    }

    public synchronized void incrementHits() {
        numberOfHits++;
        lastModified = System.currentTimeMillis();
    }

    @JsonIgnore
    public synchronized long getValue() {
        return numberOfHits;
    }
}
