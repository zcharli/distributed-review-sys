package metrics;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import key.DRSKey;
import net.tomp2p.peers.Number160;

import java.util.List;

/**
 * Created by cli on 10/4/2016.
 */
public class MetricsCollector {
    private final ConcurrentTrackingList<Number160, TrackingContext> m_trackingCache;

    public MetricsCollector(ConcurrentTrackingList<Number160, TrackingContext> tracking) {
        m_trackingCache = tracking;
    }

    public void collectUseMetric(DRSKey key) {
        if (!m_trackingCache.containsKey(key.getLocationKey())) {
            m_trackingCache.save(key.getLocationKey(), new TrackingContext(key.getLocationKey().toIntArray()));
        } else {
            TrackingContext currentTracker = m_trackingCache.get(key.getLocationKey());
            currentTracker.numberOfHits++;
            m_trackingCache.save(key.getLocationKey(), currentTracker);
        }
    }

    public ImmutableList<Number160> getTrackedKeys() {
        return m_trackingCache.keys();
    }
}
