package core;

import config.DHTConfig;
import exceptions.InitializationFailedException;

/**
 * Created by czl on 19/09/16.
 */
public class DHTManager {

    private boolean isBootstrap;

    public DHTManager(boolean isBootstrap) throws InitializationFailedException {
        this.isBootstrap = isBootstrap;

        if (DHTConfig.BOOTSRAP_ADDR == null) {
            throw new InitializationFailedException("Bootstrap node's address was unable to be found.");
        }

        initializeProfile();
    }

    private void initializeProfile() {
        DHTProfile.init(isBootstrap);
    }
}
