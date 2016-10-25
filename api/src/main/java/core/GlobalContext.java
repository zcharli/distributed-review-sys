package core;

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

    public static GlobalContext instance() {
        if (context == null) {
            context = new GlobalContext();
        }
        return context;
    }

    private GlobalContext() {
    }

    public void setState(Queue<ProductReviewWrapper> state) {
        m_cache = state;
    }

    public Queue<ProductReviewWrapper> getState() {
        if (m_cache == null) {
            return new ConcurrentLinkedQueue<>();
        }
        return m_cache;
    }
}
