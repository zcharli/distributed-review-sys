package servlet.rest;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import config.DHTConfig;
import core.DHTManager;
import core.GlobalContext;
import error.GenericReply;
import key.DRSKey;
import key.DefaultDHTKeyPair;
import msg.AsyncComplete;
import msg.AsyncResult;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.storage.Data;
import org.apache.commons.lang3.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import review.BaseReview;
import review.ProductReviewWrapper;
import review.ReviewIdentity;
import review.comparator.ReviewTimestampComparator;
import review.request.LimitQueryParam;
import review.response.ReviewGetResponse;
import review.response.ReviewOperationComplete;
import validator.ExternalReview;
import wrapper.ProductRestWrapper;

import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by czl on 04/10/16.
 */
@Path("/review")
public class ReviewServlet {
    private final static Logger LOGGER = LoggerFactory.getLogger(ReviewServlet.class);
    private final ExecutorService m_queryWorker = Executors.newFixedThreadPool(4);

    public ReviewServlet() {
    }

    @PUT
    @Path("/new/{m_productName}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void createNewReview(final @ExternalReview BaseReview request,
                                final @Suspended AsyncResponse response,
                                final @PathParam("m_productName") String identifier) {
//        if (!identifier.equals(request.getIdentifier())) {
//            response.resume(Response.serverError().entity(new GenericReply<String>("500", "Miss match identifier ID for creating new review.")));
//            return;
//        }
        String productName = null;
        try {
            productName = WordUtils.capitalize(URLDecoder.decode(identifier, "utf-8"));
            if (productName == null) {
                throw new NullPointerException("URL decoder failed");
            }
        } catch (Exception e) {
            response.resume(Response.status(Response.Status.BAD_REQUEST).entity(new GenericReply<String>("500", "Malformed request parameter, please use utf-8 encoding.")));
        }
        request.m_productName = productName;
        Number160 locationKey = Number160.createHash(request.getIdentifier());
        Number160 newDomainKey = DHTConfig.ACCEPTANCE_DOMAIN;
        Number160 contentKey = Number160.createHash(request.getContent());
        Number640 reviewKey = new Number640(locationKey, newDomainKey, contentKey, Number160.ZERO);

        request.fillInIds(locationKey, contentKey, newDomainKey, reviewKey);
        // TODO: Validate the identifier
        // request.validateId(identifier);
        DRSKey barcodeKey = DefaultDHTKeyPair.builder()
                .locationKey(locationKey)
                .contentKey(contentKey)
                .domainKey(DHTConfig.ACCEPTANCE_DOMAIN).build();
        putReview(barcodeKey, request, response);
    }

    @PUT
    @Path("/update/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void updateReview(final @ExternalReview BaseReview request,
                             final @Suspended AsyncResponse response,
                             final @PathParam("id") String identifier) {
        if (!identifier.equals(request.getIdentifier()) || request.m_contentId == null || request.m_locationId == null || request.m_domainId == null) {
            response.resume(Response.status(Response.Status.BAD_REQUEST).entity(new GenericReply<String>("500", "Invalid request parameters")));
            return;
        }

        // TODO: Validate the identifier and the key to make sure it even exists first
        // request.validateId(identifier);
        DRSKey barcodeKey = DefaultDHTKeyPair.builder()
                .locationKey(Number160.createHash(request.getIdentifier()))
                .contentKey(Number160.createHash(request.getContent()))
                .domainKey(DHTConfig.PUBLISHED_DOMAIN).build();

        DHTManager.instance().getAllFromStorage(barcodeKey, new AsyncResult() {
            @Override
            public Integer call() throws Exception {

                if (!isSuccessful()) {
                    response.resume(Response.serverError()
                            .entity(new GenericReply<String>(
                                    "DHT-GET", "An error occurred when trying to get id " + identifier))
                            .build());
                    return 0;
                }

                if (payload().size() > 0) {
                    putReview(barcodeKey, request, response);
                } else {
                    response.resume(Response.ok(new GenericReply<String>("500", "Could not find the respective review to update")).build());
                }

                return 0;
            }
        });
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("accept/{identifier}")
    public void acceptReviewIntoPublished(final @ExternalReview BaseReview request,
                                          final @Suspended AsyncResponse response,
                                          final @PathParam("identifier") String identifier) {
        if (Strings.isNullOrEmpty(identifier)) {
            response.resume(Response.serverError().entity(new GenericReply<String>("404", "No identifier in request was found")));
            return;
        }

        // TODO: handle fail case where identifier has already been approved or does not exist, atm it will never end cause of this
        Number160 locationKey = Number160.createHash(identifier);
        Number160 newDomainKey = DHTConfig.ACCEPTANCE_DOMAIN;
        Number160 contentKey = Number160.createHash(request.getContent());
        DRSKey reviewKey = DefaultDHTKeyPair.builder()
                .locationKey(locationKey)
                .contentKey(contentKey)
                .domainKey(newDomainKey)
                .build();
        // The original review is updated since the only time editing is allowed is during acceptance.
        Number640 fullKey = new Number640(locationKey, DHTConfig.PUBLISHED_DOMAIN, contentKey, Number160.ZERO);
        request.fillInIds(locationKey, contentKey, newDomainKey, fullKey);
        DHTManager.instance().approveData(reviewKey, new AsyncComplete() {
            @Override
            public Integer call() {
                if (!isSuccessful()) {
                    response.resume(Response.serverError().entity(
                            new GenericReply<String>("DHT-ACCEPT", message())
                    ).build());
                } else {
                    response.resume(Response.ok().entity(
                            new ReviewOperationComplete<String>("200", "Success")
                    ).build());
                }
                return 0;
            }
        });
    }

    @GET
    @Path("get/{identifier}")
    @Produces(MediaType.APPLICATION_JSON)
    public void getReview(final @PathParam("identifier") String identifier,
                          final @BeanParam LimitQueryParam limit,
                          final @Suspended AsyncResponse response) {

        DRSKey reviewKey = DefaultDHTKeyPair.builder()
                .locationKey(Number160.createHash(identifier))
                .domainKey(DHTConfig.PUBLISHED_DOMAIN)
                .build();
        DHTManager.instance().getAllFromStorage(reviewKey, new AsyncResult() {
            @Override
            public Integer call() throws Exception {

                if (!isSuccessful()) {
                    response.resume(Response.serverError()
                            .entity(new GenericReply<String>(
                                    "DHT-GET", "An error occurred when trying to get id " + identifier))
                            .build());
                    return 0;
                }

                List<BaseReview> allReviews = new ArrayList<BaseReview>();
                List<BaseReview> limitedReviews = new LinkedList<BaseReview>();
                for (Map.Entry<Number640, Data> results : payload().entrySet()) {
                    allReviews.add(((ReviewIdentity) (results.getValue().object())).identity());
                }

                Collections.sort(allReviews, new ReviewTimestampComparator());

                int max = limit.page * limit.step;
                int start = (limit.page - 1) * limit.step;
                if (max > allReviews.size()) {
                    max = allReviews.size();
                }
                for (int i = start; i < max; i++) {
                    limitedReviews.add(allReviews.get(i));
                }
                response.resume(Response.ok(new ReviewGetResponse(200, limitedReviews)).build());
                return 0;
            }
        });
    }

    @GET
    @Consumes({MediaType.APPLICATION_JSON, "application/vnd.api+json"})
    @Produces({MediaType.APPLICATION_JSON, "application/vnd.api+json"})
    @Path("approval")
    public void getTrackedReviewsNeedingApproval(final @Suspended AsyncResponse response) {
        ImmutableList<Number160> trackedIds = DHTManager.instance().getTrackedFromAcceptanceDomain();
        if (trackedIds == null) {
            response.resume(Response.serverError().entity(new GenericReply<String>("DHT-APPROVAL", "Unabled to find tracked IDs on this node.")));
        }
//        List<BaseReview> results = new LinkedList<>();
//        for (Number160 locationId : trackedIds) {
//            DRSKey instanceKey = DefaultDHTKeyPair.builder().locationKey(locationId).domainKey(DHTConfig.ACCEPTANCE_DOMAIN).build();
//            for (Data result : DHTManager.instance().getAllFromStorage(instanceKey)) {
//                try {
//                    results.add(((ReviewIdentity)result.object()).identity());
//                } catch (Exception e) {
//                    response.resume(Response.serverError().entity(
//                            new GenericReply<String>("DHT-APPROVAL", "Failed to extract data from DHT.")
//                    ).build());
//                }
//            }
//        }
//        response.resume(Response.ok().entity(new ReviewOperationComplete<List<BaseReview>>("200",results)).build());
        final List<BaseReview> reviewsInAcceptance = new LinkedList<>();
        final CompletableFuture<?>[] allAcceptanceReviews = trackedIds.stream()
                .map(key -> CompletableFuture.runAsync(() -> {
                    final Collection<Data> reviews = DHTManager.instance()
                            .getAllFromStorage(DefaultDHTKeyPair.builder()
                                    .locationKey(key)
                                    .domainKey(DHTConfig.ACCEPTANCE_DOMAIN)
                                    .build());
                    if (reviews != null) {
                        reviews.forEach(review -> {
                            try {
                                final BaseReview basePointer = (BaseReview) review.object();
                                reviewsInAcceptance.add(basePointer);
                            } catch (Exception e) {
                                LOGGER.error("Exception when trying to retrieve acceptance review object from Data: " + e.getMessage());
                            }
                        });
                    }
                }, m_queryWorker).exceptionally(ex -> {
                    LOGGER.error("An error occured when fetch all acceptance reviews from locations: " + ex.getMessage());
                    ex.printStackTrace();
                    return null;
                }))
                .toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(allAcceptanceReviews).join();
        response.resume(Response.ok().entity(new ReviewGetResponse(200, reviewsInAcceptance)).build());

    }

    @GET
    @Path("/check")
    public void checkProductId() {

    }

    @GET
    @Path("/ping")
    public String pong() {
        return "pong";
    }

    private void putReview(DRSKey barcodeKey, BaseReview request, AsyncResponse response) {
        DHTManager.instance().putContentOnStorage(barcodeKey, request, new AsyncComplete() {
            @Override
            public Integer call() {
                if (!isSuccessful()) {
                    response.resume(Response.serverError()
                            .entity(new GenericReply<String>(
                                    "DHT-PUT", "An error occurred when trying to put object into the DHT and thus has failed"))
                            .build());
                } else {
                    response.resume(Response.ok(new ReviewOperationComplete<>("DHT-PUT", "Success")).build());
                }
                return 0;
            }
        });
    }
}
