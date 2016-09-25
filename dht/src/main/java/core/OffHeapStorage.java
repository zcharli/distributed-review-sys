package core;

import net.tomp2p.dht.Storage;
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
public class OffHeapStorage implements Storage {

    // Core
    private static final JedisPool m_storagePool = new JedisPool(new JedisPoolConfig(), "localhost");
    // Think about guava LoadingCache

    // Maintenance

    // Protection

    // Responsibility

    // Core storage
    @Override
    public Data put(Number640 key, Data value) {
        Data newData = null;
        try (Jedis adapter = m_storagePool.getResource()) {

        }
        return newData;
    }

    @Override
    public Data get(Number640 key){
        return null;
    }

    @Override
    public boolean contains(Number640 key){
        return true;
    }

    @Override
    public int contains(Number640 from, Number640 to){
        return 0;
    }

    @Override
    public Data remove(Number640 key, boolean returnData){
        return null;
    }

    @Override
    public NavigableMap<Number640, Data> remove(Number640 from, Number640 to){
        return null;
    }

    public NavigableMap<Number640, Data> subMap(Number640 from, Number640 to, int limit, boolean ascending){
        return null;
    }

    @Override
    public NavigableMap<Number640, Data> map(){
        return null;
    }

    @Override
    public void close(){
        return;
    }

    // Maintenance
    @Override
    public void addTimeout(Number640 key, long expiration) {
        return;
    }

    @Override
    public void removeTimeout(Number640 key) {
        return;
    }

    @Override
    public Collection<Number640> subMapTimeout(long to) {
        return null;
    }

    @Override
    public int storageCheckIntervalMillis() {
        return 0;
    }

    // Domain / entry protection
    @Override
    public boolean protectDomain(Number320 key, PublicKey publicKey) {
        return true;
    }

    @Override
    public boolean isDomainProtectedByOthers(Number320 key, PublicKey publicKey) {
        return true;
    }

    @Override
    public boolean protectEntry(Number480 key, PublicKey publicKey) {
        return true;
    }

    @Override
    public boolean isEntryProtectedByOthers(Number480 key, PublicKey publicKey) {
        return true;
    }

    // Responsibility
    @Override
    public Number160 findPeerIDsForResponsibleContent(Number160 locationKey) {
        return null;
    }

    @Override
    public Collection<Number160> findContentForResponsiblePeerID(Number160 peerID) {
        return null;
    }

    @Override
    public boolean updateResponsibilities(Number160 locationKey, Number160 peerId) {
        return true;
    }

    @Override
    public void removeResponsibility(Number160 locationKey) {
        return;
    }
}
