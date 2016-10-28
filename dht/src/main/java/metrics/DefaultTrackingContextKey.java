package metrics;

import net.tomp2p.peers.Number160;

/**
 * Created by cli on 10/28/2016.
 */
public class DefaultTrackingContextKey implements TrackingContextView {

    private Number160 locationKey;
    private MetricsCollector.TrackingType trackingType;

    public DefaultTrackingContextKey(Number160 loc, MetricsCollector.TrackingType type) {
        locationKey = loc;
        trackingType = type;
    }

    public Number160 getPrimary() {
        return locationKey;
    }

    public MetricsCollector.TrackingType getType() {
        return trackingType;
    }
}
