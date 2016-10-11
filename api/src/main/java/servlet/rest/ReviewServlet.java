package servlet.rest;

import com.google.common.collect.ImmutableList;
import config.DHTConfig;
import core.DHTManager;
import error.GenericReply;
import key.DRSKey;
import key.DefaultDHTKeyPair;
import msg.AsyncComplete;
import msg.AsyncResult;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.storage.Data;
import org.glassfish.jersey.server.ManagedAsync;
import review.BaseReview;
import review.ReviewIdentity;
import review.comparator.ReviewTimestampComparator;
import review.request.BaseCRRequest;
import review.request.LimitQueryParam;
import review.response.ReviewGetResponse;
import review.response.ReviewOperationComplete;
import validator.ExternalReview;

import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

/**
 * Created by czl on 04/10/16.
 */
@Path("/review")
public class ReviewServlet {

    public ReviewServlet() {
    }

    @PUT
    @Path("/new/{identifier}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void createNewReview(final @ExternalReview BaseCRRequest request,
                                final @Suspended AsyncResponse response,
                                final @PathParam("identifier") String identifier) {

        // TODO: Validate the identifier
        // request.validateId(identifier);

        BaseReview reviewToSave = request.buildReview();
        DRSKey barcodeKey = DefaultDHTKeyPair.builder()
                .locationKey(Number160.createHash(reviewToSave.getIdentifier()))
                .contentKey(Number160.createHash(reviewToSave.getContent()))
                .domainKey(DHTConfig.ACCEPTANCE_DOMAIN).build();
        DHTManager.instance().putContentOnStorage(barcodeKey, reviewToSave, new AsyncComplete() {
            @Override
            public Integer call() {
                if (!isSuccessful()) {
                    response.resume(Response.serverError()
                            .entity(new GenericReply<String>(
                                    "DHT-PUT", "An error occurred when trying to put object into the DHT and thus has failed"))
                            .build());
                } else {
                    response.resume(Response.ok(new GenericReply<String>("DHT-PUT", "Success")).build());
                }
                return 0;
            }
        });
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("accept/{identifier}")
    public void acceptReviewIntoPublished(final @ExternalReview BaseCRRequest request,
                                          final @Suspended AsyncResponse response,
                                          final @PathParam("identifier") String identifier) {
        DRSKey reviewKey = DefaultDHTKeyPair.builder()
                .locationKey( Number160.createHash(identifier) )
                .contentKey( Number160.createHash(request.content) )
                .domainKey(DHTConfig.ACCEPTANCE_DOMAIN)
                .build();
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
                .locationKey( Number160.createHash(identifier) )
                .domainKey( DHTConfig.PUBLISHED_DOMAIN )
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
                    allReviews.add(((ReviewIdentity)(results.getValue().object())).identity());
                }

                Collections.sort(allReviews, new ReviewTimestampComparator());

                int max = limit.page * limit.step;
                int start = (limit.page - 1) * limit.step;
                if ( max > allReviews.size() ) {
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
    @Produces(MediaType.APPLICATION_JSON)
    @Path("approval")
    public void getTrackedReviewsNeedingApproval(final @Suspended AsyncResponse response) {
        ImmutableList<Number160> trackedIds = DHTManager.instance().getTrackedFromAcceptanceDomain();
        if (trackedIds == null) {
            response.resume(Response.serverError().entity(new GenericReply<String>("DHT-APPROVAL", "Unabled to find tracked IDs on this node.")));
        }
        List<BaseReview> results = new LinkedList<>();
        for (Number160 locationId : trackedIds) {
            DRSKey instanceKey = DefaultDHTKeyPair.builder().locationKey(locationId).domainKey(DHTConfig.ACCEPTANCE_DOMAIN).build();
            for (Data result : DHTManager.instance().getAllFromStorage(instanceKey)) {
                try {
                    results.add(((ReviewIdentity)result.object()).identity());
                } catch (Exception e) {
                    response.resume(Response.serverError().entity(
                            new GenericReply<String>("DHT-APPROVAL", "Failed to extract data from DHT.")
                    ).build());
                }
            }
        }
        response.resume(Response.ok().entity(new ReviewOperationComplete<List<BaseReview>>("200",results)).build());
    }


    @GET
    @Path("/ping")
    public String pong() {
        return "pong";
    }
}
