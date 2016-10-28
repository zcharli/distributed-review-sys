package metrics;

import com.google.common.collect.ImmutableList;
import key.DRSKey;
import net.tomp2p.peers.Number160;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by cli on 10/4/2016.
 */
public class MetricsCollector {

    public static enum TrackingType {
        USAGE
    }

    private final int NUM_TRACKING_TYPES;

    private final static Logger LOGGER = LoggerFactory.getLogger(MetricsCollector.class);

    private final ConcurrentTrackingList m_trackingCache;

    public MetricsCollector() {
        NUM_TRACKING_TYPES = TrackingType.values().length;
        m_trackingCache = new ConcurrentTrackingList();
    }

    public MetricsCollector(ConcurrentTrackingList tracking) {
        m_trackingCache = tracking;
        NUM_TRACKING_TYPES = TrackingType.values().length;
    }

    public MetricsCollector(List<Object> fromDisk) {
        this();
        for (Object entry : fromDisk) {
            try {
                Tuple<Number160, TrackingContext[]> castedEntry = (Tuple<Number160, TrackingContext[]>)entry;
                m_trackingCache.silentLoad(castedEntry.a, castedEntry.b);
            } catch (Exception e) {
                LOGGER.error("Error during casting metrics collector when pulled info from persistant storage.");
            }
        }
    }

    public void collectUseMetric(DRSKey key) {
        if (!m_trackingCache.containsKey(key.getLocationKey())) {
            TrackingContext[] trackingContexts = new TrackingContext[NUM_TRACKING_TYPES];
            trackingContexts[TrackingType.USAGE.ordinal()] =  new TrackingContext(key.getLocationKey(), TrackingType.USAGE);
            m_trackingCache.save(key.getLocationKey(), trackingContexts);
        } else {
            TrackingContext[] currentTracker = m_trackingCache.get(key.getLocationKey());
            if (currentTracker.length < NUM_TRACKING_TYPES) {
                TrackingContext[] tempTracker = new TrackingContext[NUM_TRACKING_TYPES];
                for (int i = 0; i< NUM_TRACKING_TYPES; i++) {
                    tempTracker[i] = currentTracker[i];
                }
                currentTracker = tempTracker;
            }
            if (currentTracker[TrackingType.USAGE.ordinal()] == null) {
                currentTracker[TrackingType.USAGE.ordinal()] =  new TrackingContext(key.getLocationKey(), TrackingType.USAGE);
            }
            currentTracker[TrackingType.USAGE.ordinal()].incrementHits();
            m_trackingCache.save(key.getLocationKey(), currentTracker);
        }
    }

    public ImmutableList<Number160> getTrackedKeys() {
        return m_trackingCache.keys();
    }
}
