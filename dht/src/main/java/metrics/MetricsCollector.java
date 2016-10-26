package metrics;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import core.DHTManager;
import key.DRSKey;
import net.tomp2p.peers.Number160;
import org.mapdb.Fun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by cli on 10/4/2016.
 */
public class MetricsCollector {
    private final static Logger LOGGER = LoggerFactory.getLogger(MetricsCollector.class);

    private final ConcurrentTrackingList<Number160, TrackingContext> m_trackingCache;

    public MetricsCollector(ConcurrentTrackingList<Number160, TrackingContext> tracking) {
        m_trackingCache = tracking;
    }

    public MetricsCollector(List<Object> fromDisk) {
        m_trackingCache = new ConcurrentTrackingList<>();
        for (Object entry : fromDisk) {
            try {
                Fun.Tuple2<Number160, TrackingContext> castedEntry = (Fun.Tuple2<Number160, TrackingContext>)entry;
                m_trackingCache.silentLoad(castedEntry.a, castedEntry.b);
            } catch (Exception e) {
                LOGGER.error("Error during casting metrics collector when pulled info from persistant storage.");
            }
        }
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
