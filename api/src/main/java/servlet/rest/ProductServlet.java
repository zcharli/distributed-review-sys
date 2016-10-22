package servlet.rest;

import com.google.common.base.Strings;
import config.DHTConfig;
import core.DHTManager;
import error.GenericReply;
import key.DefaultDHTKeyPair;
import net.tomp2p.storage.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import review.BaseReview;
import review.ProductReviewWrapper;
import wrapper.ProductRestWrapper;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;
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
    @Consumes({MediaType.APPLICATION_JSON, "application/vnd.api+json"})
    @Produces({MediaType.APPLICATION_JSON, "application/vnd.api+json"})
    public void getAllProducts(final @Suspended AsyncResponse response) {

        Queue<ProductReviewWrapper> productList = new ConcurrentLinkedQueue<ProductReviewWrapper>();
        // TODO: Limit the number of possible keys we can fetch at a time, say MAX 10
        final CompletableFuture<?> fetchAllProducts = CompletableFuture.supplyAsync(() -> DHTManager.instance().getKeysFromKeyStore(), m_queryWorker)
                .thenApply(locationKeys -> {
                            final CompletableFuture<?>[] productFutures = locationKeys.stream()
                                    .map(key -> CompletableFuture.runAsync(() -> {
                                        final ProductReviewWrapper product = new ProductReviewWrapper();
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
                                                    if (!Strings.isNullOrEmpty(basePointer.m_productName) && Strings.isNullOrEmpty(product.name)) {
                                                        product.setName(basePointer.m_productName);
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
                            response.resume(Response.ok().entity(new ProductRestWrapper().setProducts(productList)).build());
                            return productList;
                        }
                ).exceptionally(ex -> {
                    response.resume(Response.serverError().entity(new GenericReply<String>("500", "An error occured: " + ex.getMessage())).build());
                    return productList;
                });


//    @GET
//    @Path("/all")
//    @Consumes({MediaType.APPLICATION_JSON, "application/vnd.api+json"})
//    @Produces({MediaType.APPLICATION_JSON, "application/vnd.api+json"})
//    public void getAllProducts(@Context HttpServletRequest httpServletRequest, final @Suspended AsyncResponse response) {
//        // Backend specific Route
//
//        final String requestUri = httpServletRequest.getRequestURL().toString();
//        final Queue<JsonApiFormatTuple<JsonApiWrapper, List<Map<String, Object>>>> jsonApiModel = new ConcurrentLinkedQueue<>();
//        // TODO: Limit the number of possible keys we can fetch at a time, say MAX 10
//        final CompletableFuture<?> fetchAllProducts = CompletableFuture.supplyAsync(() -> DHTManager.instance().getKeysFromKeyStore(), m_queryWorker)
//                .thenApply(locationKeys -> {
//                            // Not all products have names, so we try to find it if someone entered it, create wrapper here to attach name
//                            final JsonApiTopLevel topLevelJsonResponse = new JsonApiTopLevel();
//                            final CompletableFuture<?>[] productFutures = locationKeys.stream()
//                                    .map(key -> CompletableFuture.runAsync(() -> {
//                                        // TODO: look into returning just a fixed number of reviews to paginate
//                                        final Collection<Data> reviews = DHTManager.instance()
//                                                .getAllFromStorage(DefaultDHTKeyPair.builder()
//                                                        .locationKey(key)
//                                                        .domainKey(DHTConfig.PUBLISHED_DOMAIN)
//                                                        .build());
//                                        if (reviews != null) {
//                                            final JsonApiWrapper jsonDataDefinition = new JsonApiWrapper()
//                                                    .setModelId(key.toString())
//                                                    .setModelType("product")
//                                                    .setRequestUri(requestUri);
//                                            List<Map<String, Object>> modelAttrRepresentation = new LinkedList<Map<String, Object>>();
//
//                                            reviews.forEach(review -> {
//                                                try {
//                                                    final BaseReview basePointer = (BaseReview) review.object();
//                                                    if (basePointer != null) {
//                                                        if (!Strings.isNullOrEmpty(basePointer.m_productName) && !jsonDataDefinition.checkHasName()) {
//                                                            // TODO: Maybe we need to create a rule on how we select possible product names
//                                                            jsonDataDefinition.putAttribute("name", basePointer.m_productName);
//                                                        }
//                                                        JsonApiFormatTuple.JsonApiShortRelationshipRep relationship = new JsonApiFormatTuple.JsonApiShortRelationshipRep("review", basePointer.getAbsoluteId());
//                                                        jsonDataDefinition.addToRelationshipData("review", relationship);
//                                                        modelAttrRepresentation.add(basePointer.mapObjectForEmber(relationship));
//                                                    }
//                                                } catch (Exception e) {
//                                                    LOGGER.error("Exception when trying to retrieve review object from Data: " + e.getMessage());
//                                                }
//                                            });
//                                            // Adding this here for thread safety
//                                            JsonApiFormatTuple<JsonApiWrapper, List<Map<String, Object>>> packagePayload = new JsonApiFormatTuple<>(jsonDataDefinition, modelAttrRepresentation);
//                                            jsonApiModel.add(packagePayload);
//                                        }
//                                    }, m_queryWorker).exceptionally(ex -> {
//                                        LOGGER.error("An error occured when fetch all reviews from locations: " + ex.getMessage());
//                                        ex.printStackTrace();
//                                        return null;
//                                    }))
//                                    .toArray(CompletableFuture[]::new);
//                            CompletableFuture.allOf(productFutures).join();
//
//                            // Only reason this is set here and not built by builder is cause we needed to set name attribute
//                            List<JsonApiFormatTuple.JsonApiShortRelationshipRep> relationshipRepList = new LinkedList<JsonApiFormatTuple.JsonApiShortRelationshipRep>();
//                            for (JsonApiFormatTuple<JsonApiWrapper, List<Map<String, Object>>> modelMapping : jsonApiModel) {
//                                topLevelJsonResponse.addData(modelMapping.shortRepresentation);
//                                topLevelJsonResponse.addPayload(modelMapping.fullRepresentation);
//                            }
//                            response.resume(Response.ok().entity(topLevelJsonResponse).build());
//                            return jsonApiModel;
//                        }
//                ).exceptionally(ex -> {
//                    response.resume(Response.serverError().entity(new GenericReply<String>("500", "An error occured: " + ex.getMessage())).build());
//                    return jsonApiModel;
//                });
//    }
<<<<<<< HEAD
    }
=======

>>>>>>> Fix up rest vs json api
}

//    }
