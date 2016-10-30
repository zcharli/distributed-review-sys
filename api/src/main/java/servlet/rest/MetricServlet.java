package servlet.rest;

import config.APIConfig;
import core.DHTManager;
import core.GlobalContext;
import metric.*;
import metrics.MetricsCollector;
import metrics.TrackingContext;
import net.tomp2p.peers.Number160;
import org.apache.commons.lang3.text.WordUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import review.BaseReview;
import review.ProductReviewWrapper;
import review.response.metric.MetricPayloadRestWrapper;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by cli on 10/28/2016.
 * <p>
 * This class needs a refactor. Mainly decouple each function and move to another class
 */
@Path("/metric")
public class MetricServlet {
    private final static Logger LOGGER = LoggerFactory.getLogger(MetricServlet.class);

    private final ExecutorService m_queryWorker = Executors.newFixedThreadPool(10);

    @GET
    @Path("/all")
    @Produces({MediaType.APPLICATION_JSON, "application/vnd.api+json"})
    public void getFrontPageMetrics(final @Suspended AsyncResponse response) {
        // 1. Average review per product
        // 2. Total number of products tracked
        // 3. Total number of reviews tracked
        // 4. Acceptance Rate
        // 5. Average length of review
        // 6. Average product stars

        // 7. Reviews submitted by last week 9 (implement)

        // 8. Top 10 most viewed products
        // 9. Top 10 most upvoted products
        // 10. Top 10 newest approved reviews

        // Percent of product types
        // Average number of stars per product type
        // Amount of disk space used

        final Queue<BaseMetric> metricsList = GlobalContext.instance().getMetricState();
        if (metricsList.size() > 0) {
            response.resume(Response.ok().entity(new MetricPayloadRestWrapper().setMetrics(metricsList)).build());
            return;
        }

        final Queue<ProductReviewWrapper> productList = GlobalContext.instance().getProductState();
        if (productList.size() == 0) {
            Collection<Number160> locationKeys = DHTManager.instance().getKeysFromKeyStore();
            ProductServlet.getAllReviewsForProduct(locationKeys, productList, m_queryWorker);
        }

        if (productList.size() == 0) {
            // there was no elements...
            response.resume(Response.ok().entity(new MetricPayloadRestWrapper().setMetrics(metricsList)).build());
            return;
        }

        final MetricsCollector metricsCollector = DHTManager.instance().getMetrics();
        DateTime today = getDate(System.currentTimeMillis());
        DateTime now = DateTime.now();
        final int showDays = APIConfig.SHOW_REVIEW_FOR_LAST_X_DAYS;
        int[] lastWeek = new int[showDays];
        String[] lastWeekLabels = new String[showDays];
        DateTimeFormatter fmt = DateTimeFormat.forPattern("EEEE, MMMM d, YYYY");
        for (int i = 0; i < lastWeek.length; ++i) {
            lastWeekLabels[i] = now.minusDays(showDays - i - 1).toString(fmt);
        }
        PriorityQueue<PreviewReviewMetric> topTenMostViewed = new PriorityQueue<PreviewReviewMetric>(productList.size(),
                (first, second) -> (int) (second.value - first.value));
        PriorityQueue<PreviewReviewMetric> topTenUpvoted = new PriorityQueue<>(productList.size(),
                (first, second) -> (int) (second.value - first.value));
        PriorityQueue<PreviewReviewMetric> topTenMostRecent = new PriorityQueue<>(productList.size(),
                (first, second) -> (int) (second.value - first.value));

        Map<String, Integer> percentProductTypes = new HashMap<>();
        Map<String, Integer> averageStarPerProductType = new HashMap<>();
        for (String type : APIConfig.LIVE_PRODUCT_TYPES) {
            percentProductTypes.put(type, 0);
            averageStarPerProductType.put(type, 0);
        }

        int totalReviews = 0; // divide this by productList.size() to get 1
        int totalWordLength = 0; // divide this by totalReviews to get 5
        float averageStars = 0.0f;
        Number160 locationKey = null;
        for (ProductReviewWrapper product : productList) {
            totalReviews += product.reviews.size();
            float starsPerProduct = 0.0f;
            if (percentProductTypes.containsKey(product.type)) {
                percentProductTypes.put(product.type, percentProductTypes.get(product.type) + 1);
            }
            if (product.reviews.size() > 0) {
                BaseReview firstReview = product.reviews.iterator().next();
                if (firstReview.m_locationId != null) {
                    locationKey = firstReview.m_locationId;
                    TrackingContext[] trackingContexts = metricsCollector.getTrackingContext(locationKey);
                    TrackingContext usageTracking = trackingContexts[MetricsCollector.TrackingType.USAGE.ordinal()];
                    if (usageTracking != null) {
                        long views = usageTracking.getValue();
                        topTenMostViewed.offer(new PreviewReviewMetric(product, views));
                    }
                }
                locationKey = null;
            }
            int productViews = 0;
            for (BaseReview review : product.reviews) {
                totalWordLength += review.m_content.length();
                starsPerProduct += review.m_stars;
                productViews += review.m_upvotes;
                DateTime reviewDate = getDate(review.m_publishTime);
                int days = Days.daysBetween(reviewDate.toLocalDate(), today.toLocalDate()).getDays();
                if (days < showDays) {
                    int index = showDays - days - 1;
                    lastWeek[index]++;
                }
                if (averageStarPerProductType.containsKey(review.getType())) {
                    averageStarPerProductType.put(review.getType(), averageStarPerProductType.get(review.getType()) + 1);
                }
                topTenMostRecent.offer(new PreviewReviewMetric(review, review.m_publishTime));
            }

            topTenUpvoted.offer(new PreviewReviewMetric(product, productViews));
            averageStars += starsPerProduct / (float) product.reviews.size();
        }

        String[] productPercentLabels = new String[percentProductTypes.size()];
        String[] averageStarsProductLabels = new String[averageStarPerProductType.size()];
        String[] diskSpaceUsedLabel = new String[]{"Used space", "Free space"};
        int[] productPercentValues = new int[percentProductTypes.size()];
        int[] averageStarsProductValues = new int[averageStarPerProductType.size()];
        long[] diskSpaceUsedValues = new long[diskSpaceUsedLabel.length];
        int i = 0;
        for (Map.Entry<String, Integer> entry : percentProductTypes.entrySet()) {
            productPercentLabels[i] = WordUtils.capitalize(entry.getKey());
            productPercentValues[i] = entry.getValue().intValue();
            i++;
        }
        i = 0;
        for (Map.Entry<String, Integer> entry : averageStarPerProductType.entrySet()) {
            averageStarsProductLabels[i] = WordUtils.capitalize(entry.getKey());
            averageStarsProductValues[i] = entry.getValue().intValue();
            i++;
        }
        NumberFormat nf = NumberFormat.getNumberInstance();
        for (java.nio.file.Path root : FileSystems.getDefault().getRootDirectories()) {
            try {
                FileStore store = Files.getFileStore(root);
                String spaceUsed = nf.format(store.getTotalSpace() - store.getUsableSpace());
                diskSpaceUsedValues[0] =  store.getTotalSpace() - store.getUsableSpace();
                diskSpaceUsedValues[1] = store.getUsableSpace();
            } catch (IOException e) {
                LOGGER.error("Error querying space: " + e.toString());
            }
        }

        float avgRPP = totalReviews / productList.size();
        SingleMetric avgReviewPerProduct = new SingleMetric()
                .setMetricType(MetricType.AVG_REVIEW_PER_PRODUCT)
                .setValue(String.format("%.1f", avgRPP));
        metricsList.add(avgReviewPerProduct);

        SingleMetric totalNumProducts = new SingleMetric()
                .setMetricType(MetricType.TOTAL_PRODUCTS_TRACKED)
                .setValue(Integer.toString(productList.size()));
        metricsList.add(totalNumProducts);

        SingleMetric totalNumReviews = new SingleMetric()
                .setMetricType(MetricType.TOTAL_REVIEWS_TRACKED)
                .setValue(Integer.toString(totalReviews));
        metricsList.add(totalNumReviews);

        SingleMetric acceptanceRate = new SingleMetric()
                .setMetricType(MetricType.ACCEPTANCE_RATE)
                .setValue(String.format("%d%%",
                        (int)((float)totalReviews/ (float)(totalReviews + DHTManager.instance().getNumDeniedKeys()))*100));
        metricsList.add(acceptanceRate);

        float avgLR = totalWordLength / totalReviews;
        SingleMetric averageLenReviews = new SingleMetric()
                .setMetricType(MetricType.AVG_REVIEW_LENGTH)
                .setValue(String.format("%.1f", avgLR));
        metricsList.add(averageLenReviews);

        SingleMetric avgProductStars = new SingleMetric()
                .setMetricType(MetricType.AVG_PRODUCT_STARS)
                .setValue(String.format("%.1f/5", averageStars / (float) productList.size()));
        metricsList.add(avgProductStars);

        MultiValueMetric reviewsFromThisWeek = new MultiValueMetric()
                .setMetricType(MetricType.REVIEWS_SUBMITTED_LAST_7)
                .setLabels(lastWeekLabels)
                .setData(lastWeek);
        metricsList.add(reviewsFromThisWeek);

        ListValueMetric topTenViewed = new ListValueMetric()
                .setMetricType(MetricType.TOP_10_VIEWED_PRODUCT);
        int top10Amount = topTenMostViewed.size() < APIConfig.TOP_X_METRIC ? topTenMostViewed.size() : APIConfig.TOP_X_METRIC;
        for (i = 0; i < top10Amount; ++i) {
            topTenViewed.addReview(topTenMostViewed.poll());
        }
        metricsList.add(topTenViewed);

        ListValueMetric topTenMostUpvoted = new ListValueMetric()
                .setMetricType(MetricType.TOP_10_UPVOTED_PRODUCT);
        top10Amount = topTenUpvoted.size() < APIConfig.TOP_X_METRIC ? topTenUpvoted.size() : APIConfig.TOP_X_METRIC;
        for (i = 0; i < top10Amount; ++i) {
            topTenMostUpvoted.addReview(topTenUpvoted.poll());
        }
        metricsList.add(topTenMostUpvoted);

        ListValueMetric mostRecentReviews = new ListValueMetric()
                .setMetricType(MetricType.TOP_10_NEWEST_REVIEWS);
        top10Amount = topTenMostRecent.size() < APIConfig.TOP_X_METRIC ? topTenMostRecent.size() : APIConfig.TOP_X_METRIC;
        for (i = 0; i < top10Amount; ++i) {
            mostRecentReviews.addReview(topTenMostRecent.poll());
        }
        metricsList.add(mostRecentReviews);

        MultiValueMetric percentProductType = new MultiValueMetric()
                .setMetricType(MetricType.PERCENT_PRODUCT_TYPE)
                .setLabels(productPercentLabels)
                .setData(productPercentValues);
        metricsList.add(percentProductType);

        MultiValueMetric averageStarsPerProductType = new MultiValueMetric()
                .setMetricType(MetricType.AVG_STARS_PER_PRODUCT_TYPE)
                .setLabels(averageStarsProductLabels)
                .setData(averageStarsProductValues);
        metricsList.add(averageStarsPerProductType);

        MultiValueLongMetric diskSpaceLeft = new MultiValueLongMetric()
                .setMetricType(MetricType.AMOUNT_DISK_SPACE)
                .setLabels(diskSpaceUsedLabel)
                .setData(diskSpaceUsedValues);
        metricsList.add(diskSpaceLeft);

        response.resume(Response.ok().entity(new MetricPayloadRestWrapper().setMetrics(metricsList)).build());
        GlobalContext.instance().setMetricState(metricsList);
    }

    private DateTime getDate(final long currentTimeMillis) {
        DateTimeZone dateTimeZone = DateTimeZone.forID(APIConfig.TIMESZONE);
        return new DateTime(currentTimeMillis, dateTimeZone);
    }
}
