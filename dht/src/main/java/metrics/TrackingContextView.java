package metrics;

import net.tomp2p.peers.Number160;

/**
 * Created by cli on 10/28/2016.
 */
public interface TrackingContextView {
    Number160 getPrimary();
    MetricsCollector.TrackingType getType();
}
