package key;

import config.DHTConfig;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;

import javax.annotation.Nullable;

/**
 * Created by cli on 9/20/2016.
 */
public interface DRSKey {

    int MAX_KEY_LENGTH = 42;

    // Typical hash of an list's ID
    @Nullable
    Number160 getLocationKey();

    // Typical hash of an element of the list
    @Nullable
    Number160 getContentKey();

    @Nullable
    Number160 getDomainKey();
}
