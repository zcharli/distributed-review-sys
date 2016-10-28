package core;

import config.APIConfig;
import review.ProductReviewWrapper;
import wrapper.ProductRestWrapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by czl on 04/10/16.
 */
public class GlobalContext {

    // At least one collection will exisst containing the last product review wrapper
    private volatile Queue<ProductReviewWrapper> m_cache;
    private static GlobalContext context;
    private static long TEN_SECONDS_IN_MILI = 10000;
    private long lastModified = 0;

    public static GlobalContext instance() {
        if (context == null) {
            context = new GlobalContext();
        }
        return context;
    }

    private GlobalContext() {
    }

    public synchronized void setState(Queue<ProductReviewWrapper> state) {
        long currentTimeMillis = System.currentTimeMillis();
        if (currentTimeMillis - lastModified < TEN_SECONDS_IN_MILI) {
            return;
        }
        lastModified = currentTimeMillis;
        m_cache = state;
    }

    public synchronized Queue<ProductReviewWrapper> getState() {
        if (m_cache == null) {
            return new ConcurrentLinkedQueue<>();
        }
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastModified > APIConfig.CACHE_REFRESH_MILISECONDS) {
            m_cache = new ConcurrentLinkedQueue<>();
        }
        return m_cache;
    }
}
