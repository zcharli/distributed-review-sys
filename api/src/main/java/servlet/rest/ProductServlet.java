package servlet.rest;

import com.google.common.base.Strings;
import config.DHTConfig;
import core.DHTManager;
import error.GenericReply;
import key.DHTKeyBuilder;
import key.DefaultDHTKeyPair;
import net.tomp2p.storage.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import review.BaseReview;
import review.ProductRESTWrapper;
import review.response.GetAllProductsResponse;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by cli on 10/19/2016.
 */
@Path("/product")
public class ProductServlet {
    private final static Logger LOGGER = LoggerFactory.getLogger(ProductServlet.class);

    private final ExecutorService m_queryWorker = Executors.newFixedThreadPool(10);

    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    public void getAllProducts(final @Suspended AsyncResponse response) {

        Queue<ProductRESTWrapper> productList = new ConcurrentLinkedQueue<ProductRESTWrapper>();
        // TODO: Limit the number of possible keys we can fetch at a time, say MAX 10
        final CompletableFuture<?> fetchAllProducts = CompletableFuture.supplyAsync(() -> DHTManager.instance().getKeysFromKeyStore(), m_queryWorker)
                .thenApply(locationKeys -> {
                            final CompletableFuture<ProductRESTWrapper>[] productFutures = locationKeys.stream()
                                    .map(key -> CompletableFuture.runAsync(() -> {
                                        final ProductRESTWrapper product = new ProductRESTWrapper();
                                        // TODO: look into returning just a fixed number of reviews to paginate
                                        final Collection<Data> reviews = DHTManager.instance()
                                                .getAllFromStorage(DefaultDHTKeyPair.builder()
                                                        .locationKey(key)
                                                        .domainKey(DHTConfig.PUBLISHED_DOMAIN)
                                                        .build());
                                        if (reviews != null) {
                                            reviews.forEach(review -> {
                                                try {
                                                    final BaseReview basePointer = (BaseReview) review.object();
                                                    if (Strings.isNullOrEmpty(product.identifier)) {
                                                        product.setIdentifier(basePointer.getIdentifier());
                                                    }
                                                    product.add(basePointer);
                                                } catch (Exception e) {
                                                    LOGGER.error("Exception when trying to retrieve review object from Data: " + e.getMessage());
                                                }
                                            });
                                            productList.add(product);
                                        }
                                    }, m_queryWorker).exceptionally(ex -> {
                                        LOGGER.error("An error occured when fetch all reviews from locations: " + ex.getMessage());
                                        ex.printStackTrace();
                                        return null;
                                    }))
                                    .toArray(CompletableFuture[]::new);
                            CompletableFuture.allOf(productFutures).join();
                            response.resume(Response.ok().entity(new GetAllProductsResponse(productList)).build());
                            return productList;
                        }
                ).exceptionally(ex -> {
                    response.resume(Response.serverError().entity(new GenericReply<String>("500", "An error occured: " + ex.getMessage())).build());
                    return productList;
                });
    }

}
