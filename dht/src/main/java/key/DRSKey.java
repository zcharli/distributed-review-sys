package key;

import net.tomp2p.peers.Number160;

/**
 * Created by cli on 9/20/2016.
 */
public interface DRSKey {

    int MAX_KEY_LENGTH = 42;

    // Typical hash of an list's ID
    Number160 getLocationKey();

    // Typical hash of an element of the list
    Number160 getContentKey();

    Number160 getDomainKey();
}
