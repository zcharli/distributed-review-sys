package servlet.rest;

import core.DHTManager;
import core.GlobalContext;
import net.tomp2p.peers.Number160;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import review.ProductReviewWrapper;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by cli on 10/28/2016.
 *
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
        // Average review per product
        // Total number of products tracked
        // Total number of reviews tracked
        // Average reviews per product
        // Average length of review
        // Average product stars

        // Reviews submitted by last week 9 (implement)

        // Top 10 most viewed products
        // Top 10 most upvoted products
        // Top 10 newest approved reviews


        // Percent of product types
        // Average number of stars per product type
        // Amount of disk space used

        final Queue<ProductReviewWrapper> productList = GlobalContext.instance().getProductState();
        if (productList.size() == 0) {
            Collection<Number160> locationKeys = DHTManager.instance().getKeysFromKeyStore();
            ProductServlet.getAllReviewsForProduct(locationKeys, productList, m_queryWorker);
        }



    }
}
