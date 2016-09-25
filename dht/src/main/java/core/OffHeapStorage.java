package core;

import net.tomp2p.dht.Storage;
import net.tomp2p.dht.StorageMemory;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number320;
import net.tomp2p.peers.Number480;
import net.tomp2p.peers.Number640;
import net.tomp2p.storage.Data;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.security.PublicKey;
import java.util.Collection;
import java.util.NavigableMap;

/**
 * Created by czl on 24/09/16.
 */
public class OffHeapStorage extends StorageMemory {

    // Core
    private static final JedisPool m_storagePool = new JedisPool(new JedisPoolConfig(), "localhost");
    // Think about guava LoadingCache

    // Maintenance

    // Protection

    // Responsibility

    // Core storage
    @Override
    public Data put(Number640 key, Data value) {
        Data newData = super.put(key, value);
        try (Jedis adapter = m_storagePool.getResource()) {

        }
        return newData;
    }

    @Override
    public Data get(Number640 key){
        Data onHeap = super.get(key);
        if (onHeap == null) {
            try (Jedis adapter = m_storagePool.getResource()) {

            }
        }
        return onHeap;
    }

    @Override
    public Data remove(Number640 key, boolean returnData){
        return null;
    }

    @Override
    public NavigableMap<Number640, Data> remove(Number640 from, Number640 to){
        return null;
    }

    @Override
    public NavigableMap<Number640, Data> map(){
        return null;
    }

    @Override
    public void close(){
        super.close();
    }
}
