package core;

import config.APIConfig;
import metric.BaseMetric;
import review.ProductReviewWrapper;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by czl on 04/10/16.
 */
public class GlobalContext {

    // At least one collection will exisst containing the last product review wrapper
    private Queue<ProductReviewWrapper> m_productCache;
    private Queue<BaseMetric> m_metricCache;
    private static GlobalContext context;
    private static long TEN_SECONDS_IN_MILI = 10000;
    private long lastModifiedProduct = 0;
    private long lastModifiedMetric = 0;

    public static GlobalContext instance() {
        if (context == null) {
            context = new GlobalContext();
        }
        return context;
    }

    private GlobalContext() {}

    public synchronized void setMetricState(Queue<BaseMetric> state) {
        long currentTimeMillis = System.currentTimeMillis();
        if (currentTimeMillis - lastModifiedProduct < TEN_SECONDS_IN_MILI) {
            return;
        }
        lastModifiedMetric = currentTimeMillis;
        m_metricCache = state;
    }

    public synchronized Queue<BaseMetric> getMetricState() {
        if (m_metricCache == null) {
            return new ConcurrentLinkedQueue<>();
        }
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastModifiedMetric > APIConfig.CACHE_REFRESH_MILISECONDS) {
            m_metricCache = new ConcurrentLinkedQueue<>();
        }
        return m_metricCache;
    }

    public synchronized void setProductState(Queue<ProductReviewWrapper> state) {
        long currentTimeMillis = System.currentTimeMillis();
        if (currentTimeMillis - lastModifiedProduct < TEN_SECONDS_IN_MILI) {
            return;
        }
        lastModifiedProduct = currentTimeMillis;
        m_productCache = state;
    }

    public synchronized Queue<ProductReviewWrapper> getProductState() {
        if (m_productCache == null) {
            return new ConcurrentLinkedQueue<>();
        }
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastModifiedProduct > APIConfig.CACHE_REFRESH_MILISECONDS) {
            m_productCache = new ConcurrentLinkedQueue<>();
        }
        return m_productCache;
    }
}
