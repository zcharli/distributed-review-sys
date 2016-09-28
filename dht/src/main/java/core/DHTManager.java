package core;

import com.google.common.base.Strings;
import javax.annotation.Nullable;
import config.DHTConfig;
import exceptions.InitializationFailedException;
import key.DRSKey;
import msg.AsyncComplete;
import msg.AsyncResult;
import net.tomp2p.futures.BaseFuture;
import net.tomp2p.storage.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;

/**
 * DHTProfile must be initialized first thing before creating new DHT wrapper
 *
 * Created by czl on 19/09/16.
 */
public class DHTManager {
    private final static Logger LOGGER = LoggerFactory.getLogger(DHTManager.class);

    private final DHTProfile m_profile;
    private final DHT<DRSKey> m_dht;

    public DHTManager(boolean isBootstrap) throws InitializationFailedException {
        this(isBootstrap, true);
    }

    public DHTManager(boolean isBootstrap, boolean isPersistent) throws InitializationFailedException {

        if (DHTConfig.BOOTSRAP_ADDR == null) {
            throw new InitializationFailedException("Bootstrap node's address was unable to be found.");
        }

        m_profile = DHTProfile.init(isBootstrap, isPersistent);
        m_dht = new DHT<>();
    }

    public static DHTBuilder builder() {
        return new DHTBuilder();
    }

    public static class DHTBuilder {
        private boolean isBootstrap = false;
        private boolean isPersistent = false;
        public DHTBuilder() {}

        public DHTBuilder bootstrap(boolean yes) {
            isBootstrap = yes;
            return this;
        }

        public DHTBuilder persistent(boolean yes) {
            isPersistent = yes;
            return this;
        }

        public DHTManager build() throws InitializationFailedException {
            return new DHTManager(isBootstrap, isPersistent);
        }
    }

    public DHTConfig getGlobalConfig() {
        return DHTConfig.instance();
    }

    public boolean shutdown() {
        BaseFuture shutdownFuture = m_profile.MY_PROFILE.shutdown();
        shutdownFuture.awaitUninterruptibly();
        return shutdownFuture.isSuccess();
    }

    /**
     * Calls DHT then function returns, async result will be called back when things go right
     * @param key
     * @param asyncResult
     */
    public void getAllFromStorage(DRSKey key, AsyncResult asyncResult) {
        if (isInvalidKey(key)) {
            return;
        }

        m_dht.get(key, asyncResult);
    }

    public void addToStorage(DRSKey key, Object element, AsyncComplete asyncComplete) {
        if (isInvalidKey(key) || element == null) {
            return;
        }

        try {
            m_dht.add(key, element, asyncComplete);
        } catch (IOException e) {
            LOGGER.warn("Exception on DHT add: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void putContentOnStorage(DRSKey key, Object element, AsyncComplete asyncResult) {
        // This method shouldn't be here. Put will overwrite and we do not want that.
        if (isInvalidKey(key) || element == null) {
            return;
        }

        try {
            m_dht.put(key, element, asyncResult);
        } catch (IOException e) {
            LOGGER.warn("Exception on DHT put: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean checkActive() {
        return !m_profile.MY_PROFILE.peer().isShutdown();
    }

    @Nullable
    public DHTProfile getProfile() {
        return m_profile;
    }

    @Nullable
    public Collection<Data> getAllFromStorage(DRSKey key) {
        if (isInvalidKey(key)) {
            return null;
        }
        Collection<Data> ret = m_dht.get(key);
        return ret;
    }

    public boolean isInvalidKey(DRSKey key) {
        return (key == null ||
                Strings.isNullOrEmpty(key.getLocationKey()) ||
                key.getLocationKey().length() > DRSKey.MAX_KEY_LENGTH);
    }

    public void getContentFromStorage(DRSKey key) {
        // TODO: implement blocking get
    }

    public void putContentOnStorage(DRSKey key) {
        // TODO: Implement blocking put on DHT
    }
}
