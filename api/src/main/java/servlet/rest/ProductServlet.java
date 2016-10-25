package servlet.rest;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import config.APIConfig;
import config.DHTConfig;
import core.DHTManager;
import core.GlobalContext;
import error.GenericReply;
import key.DefaultDHTKeyPair;
import net.tomp2p.storage.Data;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import review.BaseReview;
import review.ProductReviewWrapper;
import wrapper.*;

import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Link;
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

    // product cache is reloaded per /product/all request
    private final ExecutorService m_queryWorker = Executors.newFixedThreadPool(10);

    @GET
    @Path("/all")
    @Consumes({MediaType.APPLICATION_JSON, "application/vnd.api+json"})
    @Produces({MediaType.APPLICATION_JSON, "application/vnd.api+json"})
    public void getAllProducts(final @Suspended AsyncResponse response,
                               final @QueryParam("type") String type) {

        Queue<ProductReviewWrapper> productList = new ConcurrentLinkedQueue<>();
        // TODO: Limit the number of possible keys we can fetch at a time, say MAX 10
        final CompletableFuture<?> fetchAllProducts = CompletableFuture.supplyAsync(() -> DHTManager.instance().getKeysFromKeyStore(), m_queryWorker)
                .thenApply(locationKeys -> {
                            final CompletableFuture<?>[] productFutures = locationKeys.stream()
                                    .map(key -> CompletableFuture.runAsync(() -> {
                                        final ProductReviewWrapper product = new ProductReviewWrapper().setId(key.toString());
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
                                                    if (!Strings.isNullOrEmpty(basePointer.getIdentifier()) && Strings.isNullOrEmpty(product.identifier)) {
                                                        product.setIdentifier(basePointer.getIdentifier());
                                                    }
                                                    if (!Strings.isNullOrEmpty(basePointer.getType()) && Strings.isNullOrEmpty(product.type)) {
                                                        product.setType(basePointer.getType());
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
                            GlobalContext.instance().setState(productList);
                            return productList;
                        }
                ).exceptionally(ex -> {
                    LOGGER.error("Error occurred when fetching product models /product/all: " + ex.getMessage());
                    response.resume(Response.serverError().entity(new GenericReply<String>("500", "An error occured: " + ex.getMessage())).build());
                    return productList;
                });
    }

    @GET
    @Path("/search")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void searchProducts(final @Suspended AsyncResponse response,
                               final @QueryParam("q") String query) {
        final Queue<ProductReviewWrapper> collectorRef = GlobalContext.instance().getState();

        if (collectorRef.size() == 0) {
            response.resume(Response.ok(new ProductSearchRestWrapper()).build());
        }
        final ProductSearchRestWrapper searchResults = new ProductSearchRestWrapper();

        // Fill in this search map in parallel
        final Map<String, CategorySearchResult> categories = generateSearchMap();
        // Process Type first
        final ThreadSafeCategorySearchResult typeCat = categories.get("Type");
        for (String liveTypes : APIConfig.LIVE_PRODUCT_TYPES) {
            if (liveTypes.contains(query)) {
                typeCat.addCategory(new CategorySearchResultDescription()
                        .setTitle(liveTypes.substring(0, 1).toUpperCase() + liveTypes.substring(1))
                        .setDescription("Search for " + liveTypes + " products")
                        .setURL("/product?type=" + liveTypes));
            }
        }

        final CompletableFuture<?>[] searchAllProducts = collectorRef.stream()
                .map(product -> CompletableFuture.runAsync(() -> {
                    for (String category : APIConfig.CURRENT_SEARCH_CATEGORIES) {
                        if (Strings.isNullOrEmpty(category)) {
                            LOGGER.error("Category was null during searchAllProducts routine");
                        }
                        final ThreadSafeCategorySearchResult categoryResults = categories.get(category);
                        switch (category) {
                            case "Product ID":
                                final CompletableFuture<?>[] searchAllReviewIDs = product.reviews.stream()
                                        .map(review -> CompletableFuture.runAsync(() -> {
                                            final String productRoute = "product.show";// + product.identifier + "/inspect/" + review.getContentId();
                                            if (!Strings.isNullOrEmpty(review.getIdentifier()) && StringUtils.containsIgnoreCase(review.getIdentifier(), query)) {
                                                categoryResults.addCategory(new CategorySearchResultDescription()
                                                        .setTitle("Matched Identifier")
                                                        .setDescription(review.getIdentifier())
                                                        .setURL(productRoute)
                                                        .setModel("product")
                                                        .setParam(review.getLocationId()));
                                            } else if (!Strings.isNullOrEmpty(review.getAbsoluteId()) && StringUtils.containsIgnoreCase(review.getAbsoluteId(), query)) {
                                                categoryResults.addCategory(new CategorySearchResultDescription()
                                                        .setTitle("Matched Absolute ID")
                                                        .setDescription(review.getAbsoluteId())
                                                        .setURL(productRoute)
                                                        .setModel("product")
                                                        .setParam(review.getLocationId()));
                                            } else if (!Strings.isNullOrEmpty(review.getContentId()) && StringUtils.containsIgnoreCase(review.getContentId(), query)) {
                                                categoryResults.addCategory(new CategorySearchResultDescription()
                                                        .setTitle("Matched Content Hash")
                                                        .setDescription(review.getContentId())
                                                        .setURL(productRoute)
                                                        .setModel("product")
                                                        .setParam(review.getLocationId()));
                                            } else if (!Strings.isNullOrEmpty(review.getLocationId()) && StringUtils.containsIgnoreCase(review.getLocationId(), query)) {
                                                categoryResults.addCategory(new CategorySearchResultDescription()
                                                        .setTitle("Matched Identifier Hash")
                                                        .setDescription(review.getLocationId())
                                                        .setURL(productRoute)
                                                        .setModel("product")
                                                        .setParam(review.getLocationId()));
                                            }
                                        }, m_queryWorker)).toArray(CompletableFuture[]::new);

                                CompletableFuture.allOf(searchAllReviewIDs).join();
                                break;
                            case "Review":
                                final CompletableFuture<?>[] searchAllReviews = product.reviews.stream()
                                        .map(review -> CompletableFuture.runAsync(() -> {
                                            final String reviewURL = "product.review";// + product.identifier + "/inspect/" + review.getAbsoluteId();

//                                            else if (!Strings.isNullOrEmpty(review.m_productName) && review.m_productName.contains(query)) {
//                                                categoryResults.addCategory(new CategorySearchResultDescription()
//                                                        .setTitle("Matched Product Name")
//                                                        .setDescription(review.m_productName)
//                                                        .setURL(reviewURL));
//                                            }

                                            if (!Strings.isNullOrEmpty(review.m_content) && StringUtils.containsIgnoreCase(review.m_content, query)) {
                                                categoryResults.addCategory(new CategorySearchResultDescription()
                                                        .setTitle("Matched Review")
                                                        .setDescription(review.m_content)
                                                        .setURL(reviewURL)
                                                        .setModel("review")
                                                        .setParam(review.getAbsoluteId()));
                                            }
//                                            else if (!Strings.isNullOrEmpty(review.getType()) && review.getType().contains(query)) {
//                                                categoryResults.addCategory(new CategorySearchResultDescription()
//                                                        .setTitle("Matched Type")
//                                                        .setDescription(review.getType())
//                                                        .setURL(reviewURL));
//                                            }

                                            if (!Strings.isNullOrEmpty(review.m_title) && StringUtils.containsIgnoreCase(review.m_title, query)) {
                                                categoryResults.addCategory(new CategorySearchResultDescription()
                                                        .setTitle("Matched Title")
                                                        .setDescription(review.m_title)
                                                        .setURL(reviewURL)
                                                        .setModel("review")
                                                        .setParam(review.getAbsoluteId()));
                                            }
                                        }, m_queryWorker)).toArray(CompletableFuture[]::new);

                                CompletableFuture.allOf(searchAllReviews).join();
                                break;
                            case "Product":
                                if (!Strings.isNullOrEmpty(product.identifier) && StringUtils.containsIgnoreCase(product.identifier, query)) {
                                    categoryResults.addCategory(new CategorySearchResultDescription()
                                            .setTitle("Matched Identifier")
                                            .setDescription(product.identifier)
                                            .setURL("product.show")
                                            .setModel("product")
                                            .setParam(product.id));
                                } else if (!Strings.isNullOrEmpty(product.id) && StringUtils.containsIgnoreCase(product.id, query)) {
                                    categoryResults.addCategory(new CategorySearchResultDescription()
                                            .setTitle("Matched ID")
                                            .setDescription(product.id)
                                            .setURL("product.show")
                                            .setModel("product")
                                            .setParam(product.id));
                                } else if (!Strings.isNullOrEmpty(product.name) && StringUtils.containsIgnoreCase(product.name, query)) {
                                    categoryResults.addCategory(new CategorySearchResultDescription()
                                            .setTitle("Matched Product Name")
                                            .setDescription(product.name)
                                            .setURL("product.show")
                                            .setModel("product")
                                            .setParam(product.id));
                                }
                                break;
                            default:
                                break;
                        }
                    }
                }, m_queryWorker).exceptionally(ex -> {
                    LOGGER.error("An error occured when searching throut products reviews from locations: " + ex.getMessage());
                    ex.printStackTrace();
                    return null;
                })).toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(searchAllProducts).join();
        searchResults.setAllCategories(categories);
        response.resume(Response.ok().entity(searchResults).build());
    }

    public Map<String, CategorySearchResult> generateSearchMap() {
        Map<String, CategorySearchResult> ret = new HashMap<>();
        for (String category : APIConfig.CURRENT_SEARCH_CATEGORIES) {
            ret.put(category, new CategorySearchResult().setDisplayName(category));
        }
        return ret;
    }
}


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
