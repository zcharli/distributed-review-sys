package key;

import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Created by cli on 10/4/2016.
 */
public class KeyRequestCollection<T extends DRSKey> {

    private LinkedList<T> m_keyCollection;

    public KeyRequestCollection() {
        m_keyCollection = new LinkedList<T>();
    }

    public void addKey(T key) {
        if (key == null) {
            return;
        }
        m_keyCollection.add(key);
    }

    public int size() {
        return m_keyCollection.size();
    }

    public Collection<Number640> toKeyCollection() {
        if (m_keyCollection.size() == 0) {
            return new LinkedList<>();
        }

        Collection<Number640> keysForRequest = new LinkedList<>();
        for (T key : m_keyCollection) {
            keysForRequest.add(new Number640(key.getLocationKey(), key.getLocationKey(), key.getLocationKey(), Number160.ZERO));
        }
        return keysForRequest;
    }
}
