package core;

import com.fasterxml.jackson.databind.ObjectMapper;
import config.DHTConfig;
import key.AcceptanceOffHeapKey;
import key.PublishedOffHeapKey;
import msg.RedisElementContainer;
import net.tomp2p.dht.Storage;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number320;
import net.tomp2p.peers.Number480;
import net.tomp2p.peers.Number640;
import net.tomp2p.storage.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.security.PublicKey;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Created by czl on 24/09/16.
 */
public class OffHeapStorage implements Storage {
    public static final int DEFAULT_STORAGE_CHECK_INTERVAL= 60 * 1000;
    public static final int DEFAULT_MAX_VERSIONS= -1;

    private static final Logger LOGGER = LoggerFactory.getLogger(OffHeapStorage.class);

    // Core
    private static final JedisPool m_storagePool = DHTConfig.REDIS_RESOURCE_POOL;
    final private NavigableMap<Number640, Data> dataMap = new ConcurrentSkipListMap<Number640, Data>();
    final private ObjectMapper objectMapper = new ObjectMapper();

    // Maintenance
    final private Map<Number640, Long> timeoutMap = new ConcurrentHashMap<Number640, Long>();
    final private ConcurrentSkipListMap<Long, Set<Number640>> timeoutMapRev = new ConcurrentSkipListMap<Long, Set<Number640>>();

    // Protection
    final private Map<Number320, PublicKey> protectedMap = new ConcurrentHashMap<Number320, PublicKey>();
    final private Map<Number480, PublicKey> entryMap = new ConcurrentHashMap<Number480, PublicKey>();

    // Responsibility
    final private Map<Number160, Number160> responsibilityMap = new ConcurrentHashMap<Number160, Number160>();
    final private Map<Number160, Set<Number160>> responsibilityMapRev = new ConcurrentHashMap<Number160, Set<Number160>>();

    final int storageCheckIntervalMillis;
    final int maxVersions;


    public OffHeapStorage() {
        this(DEFAULT_STORAGE_CHECK_INTERVAL, DEFAULT_MAX_VERSIONS);
    }

    public OffHeapStorage(int storageCheckIntervalMillis) {
        this(storageCheckIntervalMillis, DEFAULT_MAX_VERSIONS);
    }

    public OffHeapStorage(int storageCheckIntervalMillis, int maxVersions) {
        this.storageCheckIntervalMillis = storageCheckIntervalMillis;
        this.maxVersions = maxVersions;
    }

    public OffHeapStorage loadFromDisk() {
        try (Jedis adapter = m_storagePool.getResource()) {
            Set<String> allKeys = adapter.keys("*");
            for (String key : allKeys) {
                if (!key.startsWith("drs")) {
                    continue;
                }
                for (String jsonData : adapter.lrange(key, 0, -1)) {
                    try {
                        RedisElementContainer data = objectMapper.readValue(jsonData, RedisElementContainer.class);
                        Number640 mapKey = new Number640(
                                  new Number160(data.getLocationBuffer())
                                , new Number160(data.getDomainBuffer())
                                , new Number160(data.getContentBuffer())
                                , new Number160(data.getVersionBuffer()));

                        dataMap.put(mapKey, new Data(data.buffer));

                        LOGGER.debug("Loaded a element from disk: " + mapKey.toString());
                    } catch (IOException e) {
                        LOGGER.error("Failed to deserialize data json: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Redis was not found on the system. At the present time, on memory storage is not supported.");
            e.printStackTrace();
            System.exit(0);
        }
        LOGGER.debug("Finished loading elements from disk.");
        return this;
    }

    // Core
    @Override
    public Data put(Number640 key, Data value) {
        final Data oldData = dataMap.put(key, value);
        if (maxVersions > 0) {
            NavigableMap<Number640, Data> versions = dataMap.subMap(
                    new Number640(key.locationKey(), key.domainKey(), key.contentKey(), Number160.ZERO), true,
                    new Number640(key.locationKey(), key.domainKey(), key.contentKey(), Number160.MAX_VALUE), true);

            while (!versions.isEmpty()
                    && versions.firstKey().versionKey().timestamp() + maxVersions <= versions.lastKey().versionKey()
                    .timestamp()) {
                Map.Entry<Number640, Data> entry = versions.pollFirstEntry();
                Data removed = remove(entry.getKey(), false);
                if(removed != null) {
                    removed.release();
                }
                removeTimeout(entry.getKey());
            }
        }

        try (Jedis adapter = m_storagePool.getResource()) {
            try {
                String dataJson = objectMapper.writeValueAsString(RedisElementContainer.builder()
                        .setBuffer(value.toBytes())
                        .setContentBuffer(key.contentKey().toIntArray())
                        .setLocationBuffer(key.locationKey().toIntArray())
                        .setVersionBuffer(key.versionKey().toIntArray())
                        .setDomainBuffer(key.domainKey().toIntArray())
                        .build());
                // All reviews go to acceptance first
                String offHeapKey = buildOffHeapKey(key);
                adapter.lpush(offHeapKey, dataJson);
            } catch (Exception e) {
                LOGGER.error("Error when writing Data object to json: " + e.getMessage());
            }
        } catch (Exception e) {
            LOGGER.error("Unable to grab a shared resource for Jedis.");
            e.printStackTrace();
        }
        return oldData;
    }

    @Override
    public Data get(Number640 key) {
        return dataMap.get(key);
    }

    @Override
    public boolean contains(Number640 key) {
        return dataMap.containsKey(key);
    }

    @Override
    public int contains(Number640 fromKey, Number640 toKey) {
        NavigableMap<Number640, Data> tmp = dataMap.subMap(fromKey, true, toKey, true);
        return tmp.size();
    }

    private String buildOffHeapKey(Number640 key) {
        String offHeapKey;
        if (key.domainKey().equals(DHTConfig.ACCEPTANCE_DOMAIN)) {
            offHeapKey = AcceptanceOffHeapKey.builder().id(key).buildReviewKey();
        } else {
            offHeapKey = PublishedOffHeapKey.builder().id(key).buildReviewKey();
        }
        return offHeapKey;
    }

    @Override
    public Data remove(Number640 key, boolean returnData) {
        Data ret = dataMap.remove(key);
        if (ret == null) {
            return ret;
        }
        try (Jedis adapter = m_storagePool.getResource()) {
            String offHeapKey = buildOffHeapKey(key);
            List<String> elementsOffHeap = adapter.lrange(offHeapKey, 0, -1);
            int i = 0;
            for (String jsonData : elementsOffHeap) {
                try {
                    RedisElementContainer data = objectMapper.readValue(jsonData, RedisElementContainer.class);
                    if (data.getContentBuffer().equals(key.contentKey())) {
                        adapter.lrem(offHeapKey, i >= elementsOffHeap.size()/2 ? -1 : 1 ,jsonData);
                    }
                } catch (IOException e) {
                    LOGGER.error("Error when reading Data object to json: " + e.getMessage());
                } finally {
                    i++;
                }
            }
        }
        return ret;
    }

    @Override
    public NavigableMap<Number640, Data> remove(Number640 fromKey, Number640 toKey) {
        NavigableMap<Number640, Data> tmp = dataMap.subMap(fromKey, true, toKey, true);
        final NavigableMap<Number640, Data> retVal = new ConcurrentSkipListMap<Number640, Data>(tmp);
        tmp.clear();
        return retVal;
    }

    @Override
    public NavigableMap<Number640, Data> subMap(Number640 fromKey, Number640 toKey, int limit,
                                                boolean ascending) {

        final NavigableMap<Number640, Data> clone = ((ConcurrentSkipListMap<Number640, Data>)dataMap).clone();
        final NavigableMap<Number640, Data> tmp = clone.subMap(fromKey, true, toKey, true);
        final NavigableMap<Number640, Data> retVal = new TreeMap<Number640, Data>();
        if (limit < 0) {
            return ascending ? tmp : tmp.descendingMap();
        } else {
            Iterator<Map.Entry<Number640, Data>> iterator = ascending ? tmp.entrySet().iterator() : tmp
                    .descendingMap().entrySet().iterator();
            for (int i = 0; iterator.hasNext() && i < limit; i++) {
                Map.Entry<Number640, Data> entry = iterator.next();
                retVal.put(entry.getKey(), entry.getValue());
            }
        }
        return retVal;
    }

    @Override
    public NavigableMap<Number640, Data> map() {
        final NavigableMap<Number640, Data> retVal = new TreeMap<Number640, Data>();
        for(final Map.Entry<Number640, Data> entry:dataMap.entrySet()) {
            retVal.put(entry.getKey(), entry.getValue());
        }

        return retVal;
    }

    // Maintenance
    @Override
    public void addTimeout(Number640 key, long expiration) {
        Long oldExpiration = timeoutMap.put(key, expiration);
        Set<Number640> tmp = putIfAbsent2(expiration,
                Collections.newSetFromMap(new ConcurrentHashMap<Number640, Boolean>()));
        tmp.add(key);
        if (oldExpiration == null) {
            return;
        }
        removeRevTimeout(key, oldExpiration);
    }

    @Override
    public void removeTimeout(Number640 key) {
        Long expiration = timeoutMap.remove(key);
        if (expiration == null) {
            return;
        }
        removeRevTimeout(key, expiration);
    }

    private void removeRevTimeout(Number640 key, Long expiration) {
        Set<Number640> tmp = timeoutMapRev.get(expiration);
        if (tmp != null) {
            tmp.remove(key);
            if (tmp.isEmpty()) {
                timeoutMapRev.remove(expiration);
            }
        }
    }

    @Override
    public Collection<Number640> subMapTimeout(long to) {
        SortedMap<Long, Set<Number640>> tmp = timeoutMapRev.subMap(0L, to);
        Collection<Number640> toRemove = new ArrayList<Number640>();
        for (Set<Number640> set : tmp.values()) {
            toRemove.addAll(set);
        }
        return toRemove;
    }

    // Protection
    @Override
    public boolean protectDomain(Number320 key, PublicKey publicKey) {
        protectedMap.put(key, publicKey);
        return true;
    }

    @Override
    public boolean isDomainProtectedByOthers(Number320 key, PublicKey publicKey) {
        PublicKey other = protectedMap.get(key);
        if (other == null) {
            LOGGER.debug("domain {} not protected", key);
            return false;
        }
        final boolean retVal = !other.equals(publicKey);
        LOGGER.debug("domain {} protected: {}", key, retVal);
        return retVal;
    }

    private Set<Number640> putIfAbsent2(long expiration, Set<Number640> hashSet) {
        Set<Number640> timeouts = timeoutMapRev.putIfAbsent(expiration, hashSet);
        return timeouts == null ? hashSet : timeouts;
    }

    @Override
    public Number160 findPeerIDsForResponsibleContent(Number160 locationKey) {
        return responsibilityMap.get(locationKey);
    }

    @Override
    public Collection<Number160> findContentForResponsiblePeerID(Number160 peerID) {
        return responsibilityMapRev.get(peerID);
    }

    @Override
    public boolean updateResponsibilities(Number160 locationKey, Number160 peerId) {
        final Number160 oldPeerID =  responsibilityMap.put(locationKey, peerId);
        final boolean hasChanged;
        if(oldPeerID != null) {
            if(oldPeerID.equals(peerId)) {
                hasChanged = false;
            } else {
                removeRevResponsibility(oldPeerID, locationKey);
                hasChanged = true;
            }
        } else {
            hasChanged = true;
        }
        Set<Number160> contentIDs = responsibilityMapRev.get(peerId);
        if(contentIDs == null) {
            contentIDs = new HashSet<Number160>();
            responsibilityMapRev.put(peerId, contentIDs);
        }
        contentIDs.add(locationKey);
        LOGGER.debug("Update {} is responsible for key {}.", peerId, locationKey);
        return hasChanged;
    }

    @Override
    public void removeResponsibility(Number160 locationKey) {
        Number160 peerId = responsibilityMap.remove(locationKey);
        if(peerId != null) {
            removeRevResponsibility(peerId, locationKey);
            LOGGER.debug("Remove responsiblity for {}.", locationKey);
        }
    }

    private void removeRevResponsibility(Number160 peerId, Number160 locationKey) {
        Set<Number160> contentIDs = responsibilityMapRev.get(peerId);
        if (contentIDs != null) {
            contentIDs.remove(locationKey);
            if (contentIDs.isEmpty()) {
                responsibilityMapRev.remove(peerId);
            }
        }
    }

    // Misc
    @Override
    public void close() {
        dataMap.clear();
        protectedMap.clear();
        timeoutMap.clear();
        timeoutMapRev.clear();
    }

    @Override
    public boolean protectEntry(Number480 key, PublicKey publicKey) {
        entryMap.put(key, publicKey);
        return true;
    }

    @Override
    public boolean isEntryProtectedByOthers(Number480 key, PublicKey publicKey) {
        PublicKey other = entryMap.get(key);
        if (other == null) {
            return false;
        }
        return !other.equals(publicKey);
    }

    @Override
    public int storageCheckIntervalMillis() {
        return storageCheckIntervalMillis;
    }
}
