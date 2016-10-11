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

/**
 * Created by cli on 10/10/2016.
 */
public class ConcurrentTrackingList<K, V> {
    private final static Logger LOGGER = LoggerFactory.getLogger(ConcurrentTrackingList.class);


    private volatile Map<K, V> m_cache;
    private final Map<V, Integer> m_indexMap;
    private volatile ObjectMapper objectMapper;

    public ConcurrentTrackingList() {
        m_cache = new LinkedHashMap<K, V>();
        m_indexMap = new HashMap<V, Integer>();
        objectMapper = new ObjectMapper();
    }

    public void save(K key, V value) {
        if (m_cache.containsKey(key)) {
            update(key, value);
            return;
        }
        m_cache.put(key, value);
        try (Jedis adapter = DHTConfig.REDIS_RESOURCE_POOL.getResource()) {
            try {
                String buffer = objectMapper.writeValueAsString(value);
                String trackingKey = DHTConfig.TRACKED_ID + key.toString();
                adapter.lpush(trackingKey, buffer);
            } catch (Exception e) {
                LOGGER.error("Json processing error when trying to write value as string: " + key);
            }
        }
        m_indexMap.put(value, m_cache.size() - 1);
    }

    public void update(K key, V value) {
        m_cache.replace(key, value);
        try (Jedis adapter = DHTConfig.REDIS_RESOURCE_POOL.getResource()) {
            try {
                String buffer = objectMapper.writeValueAsString(value);
                adapter.lset(DHTConfig.TRACKED_ID, m_indexMap.get(value), buffer);
            } catch (Exception e) {
                LOGGER.error("Json processing error when trying to set new value as string in cache: " + key);
            }
        }
    }

    public void silentLoad(K key, V value) {
        m_cache.put(key, value);
        m_indexMap.put(value, m_cache.size() - 1);
    }

    public V get(K key) {
        return m_cache.get(key);
    }

    public boolean containsKey(K key) {
        return m_cache.containsKey(key);
    }

    public ImmutableList<K> keys() {
        return ImmutableList.copyOf(m_cache.keySet().iterator());
    }
}
