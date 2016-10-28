package metrics;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import config.DHTConfig;
import net.tomp2p.peers.Number160;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by cli on 10/10/2016.
 */
public class ConcurrentTrackingList {
    private final static Logger LOGGER = LoggerFactory.getLogger(ConcurrentTrackingList.class);

    private volatile Map<Number160, TrackingContext[]> m_cache;
    private volatile ObjectMapper objectMapper;

    public ConcurrentTrackingList() {
        m_cache = new ConcurrentHashMap<Number160, TrackingContext[]>();
        objectMapper = new ObjectMapper();
    }

    public void save(Number160 key, TrackingContext[] value) {
        if (m_cache.containsKey(key)) {
            update(key, value);
            return;
        }
        m_cache.put(key, value);
        try (Jedis adapter = DHTConfig.REDIS_RESOURCE_POOL.getResource()) {
            try {
                String buffer = objectMapper.writeValueAsString(value);
                String trackingNumber160ey = DHTConfig.TRACKED_ID + key.toString();
                adapter.lpush(trackingNumber160ey, buffer);
            } catch (Exception e) {
                LOGGER.error("Json processing error save(): "  + key + " " + e.getMessage());
            }
        }
    }

    private void update(Number160 key, TrackingContext[] value) {
        m_cache.replace(key, value);
        try (Jedis adapter = DHTConfig.REDIS_RESOURCE_POOL.getResource()) {
            try {
                String buffer = objectMapper.writeValueAsString(value);
                adapter.lset(DHTConfig.TRACKED_ID + key.toString(), 0, buffer);
            } catch (Exception e) {
                LOGGER.error("Json processing error update(): "  + key + " " + e.getMessage() + "\n");
                e.printStackTrace();
            }
        }
    }

    public void silentLoad(Number160 key, TrackingContext[] value) {
        m_cache.put(key, value);
    }

    public TrackingContext[] get(Number160 key) {
        return m_cache.get(key);
    }

    public boolean containsKey(Number160 key) {
        return m_cache.containsKey(key);
    }

    public ImmutableList<Number160> keys() {
        return ImmutableList.copyOf(m_cache.keySet().iterator());
    }
}
