package servlet.rest;

import com.google.common.base.Strings;
import config.DHTConfig;
import core.DHTManager;
import error.GenericReply;
import jsonapi.JsonApiFormatTuple;
import jsonapi.JsonApiTopLevel;
import key.DefaultDHTKeyPair;
import net.tomp2p.storage.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import review.BaseReview;
import jsonapi.JsonApiResourceWrapper;
import jsonapi.JsonApiWrapper;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
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
    public void getAllProducts(@Context HttpServletRequest httpServletRequest, final @Suspended AsyncResponse response) {
        // Backend specific Route

        final String requestUri = httpServletRequest.getRequestURL().toString();
        final Queue<JsonApiFormatTuple<JsonApiFormatTuple.JsonApiShortRelationshipRep, Map<String, Object>>> productList = new ConcurrentLinkedQueue<>();
        // TODO: Limit the number of possible keys we can fetch at a time, say MAX 10
        final CompletableFuture<?> fetchAllProducts = CompletableFuture.supplyAsync(() -> DHTManager.instance().getKeysFromKeyStore(), m_queryWorker)
                .thenApply(locationKeys -> {
                            // Not all products have names, so we try to find it if someone entered it, create jsonapi here to attach name
                            final JsonApiWrapper jsonDataDefinition = new JsonApiWrapper();
                            final JsonApiTopLevel topLevelJsonResponse = new JsonApiTopLevel();
                            final CompletableFuture<?>[] productFutures = locationKeys.stream()
                                    .map(key -> CompletableFuture.runAsync(() -> {
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
                                                    if (basePointer != null) {
                                                        if (!Strings.isNullOrEmpty(basePointer.m_productName) && !jsonDataDefinition.checkHasName()) {
                                                            // TODO: Maybe we need to create a rule on how we select possible product names
                                                            jsonDataDefinition.putAttribute("name", basePointer.m_productName);
                                                        }

                                                        JsonApiFormatTuple.JsonApiShortRelationshipRep relationship = new JsonApiFormatTuple.JsonApiShortRelationshipRep("review", basePointer.getModelId());
                                                        JsonApiFormatTuple<JsonApiFormatTuple.JsonApiShortRelationshipRep, Map<String, Object>> packagePayload = new JsonApiFormatTuple<>(
                                                                relationship,
                                                                basePointer.mapObjectForEmber(relationship)
                                                        );
                                                        // Adding this here for thread safety
                                                        productList.add(packagePayload);
                                                    }
                                                } catch (Exception e) {
                                                    LOGGER.error("Exception when trying to retrieve review object from Data: " + e.getMessage());
                                                }
                                            });
                                        }
                                    }, m_queryWorker).exceptionally(ex -> {
                                        LOGGER.error("An error occured when fetch all reviews from locations: " + ex.getMessage());
                                        ex.printStackTrace();
                                        return null;
                                    }))
                                    .toArray(CompletableFuture[]::new);
                            CompletableFuture.allOf(productFutures).join();

                            // Only reason this is set here and not built by builder is cause we needed to set name attribute
                            List<JsonApiFormatTuple.JsonApiShortRelationshipRep> relationshipRepList = new LinkedList<JsonApiFormatTuple.JsonApiShortRelationshipRep>();
                            for (JsonApiFormatTuple<JsonApiFormatTuple.JsonApiShortRelationshipRep, Map<String, Object>> tuple : productList) {
                                relationshipRepList.add(tuple.shortRepresentation);
                                topLevelJsonResponse.addPayload(tuple.fullRepresentation);
                            }
                            topLevelJsonResponse.addData(jsonDataDefinition.setRequestUri(requestUri).setRelationshipData("review", relationshipRepList));
                            response.resume(Response.ok().entity(jsonDataDefinition).build());
                            return productList;
                        }
                ).exceptionally(ex -> {
                    response.resume(Response.serverError().entity(new GenericReply<String>("500", "An error occured: " + ex.getMessage())).build());
                    return productList;
                });
    }

}

//    @GET
//    @Path("/all")
//    @Consumes({MediaType.APPLICATION_JSON, "application/vnd.api+json"})
//    @Produces({MediaType.APPLICATION_JSON, "application/vnd.api+json"})
//    public void getAllProducts(final @Suspended AsyncResponse response) {
//
//        Queue<ProductRESTWrapper> productList = new ConcurrentLinkedQueue<ProductRESTWrapper>();
//        // TODO: Limit the number of possible keys we can fetch at a time, say MAX 10
//        final CompletableFuture<?> fetchAllProducts = CompletableFuture.supplyAsync(() -> DHTManager.instance().getKeysFromKeyStore(), m_queryWorker)
//                .thenApply(locationKeys -> {
//                            final CompletableFuture<ProductRESTWrapper>[] productFutures = locationKeys.stream()
//                                    .map(key -> CompletableFuture.runAsync(() -> {
//                                        final ProductRESTWrapper product = new ProductRESTWrapper();
//                                        // TODO: look into returning just a fixed number of reviews to paginate
//                                        final Collection<Data> reviews = DHTManager.instance()
//                                                .getAllFromStorage(DefaultDHTKeyPair.builder()
//                                                        .locationKey(key)
//                                                        .domainKey(DHTConfig.PUBLISHED_DOMAIN)
//                                                        .build());
//                                        if (reviews != null) {
//                                            reviews.forEach(review -> {
//                                                try {
//                                                    final BaseReview basePointer = (BaseReview) review.object();
//                                                    if (Strings.isNullOrEmpty(product.identifier)) {
//                                                        product.setIdentifier(basePointer.getIdentifier());
//                                                    }
//                                                    product.add(basePointer);
//                                                } catch (Exception e) {
//                                                    LOGGER.error("Exception when trying to retrieve review object from Data: " + e.getMessage());
//                                                }
//                                            });
//                                            productList.add(product);
//                                        }
//                                    }, m_queryWorker).exceptionally(ex -> {
//                                        LOGGER.error("An error occured when fetch all reviews from locations: " + ex.getMessage());
//                                        ex.printStackTrace();
//                                        return null;
//                                    }))
//                                    .toArray(CompletableFuture[]::new);
//                            CompletableFuture.allOf(productFutures).join();
//                            response.resume(Response.ok().entity(new GetAllProductsResponse(productList)).build());
//                            return productList;
//                        }
//                ).exceptionally(ex -> {
//                    response.resume(Response.serverError().entity(new GenericReply<String>("500", "An error occured: " + ex.getMessage())).build());
//                    return productList;
//                });
//    }
