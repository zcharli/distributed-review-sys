package servlet.rest;

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
import review.request.BaseCRRequest;
import review.request.LimitQueryParam;
import validator.ExternalReview;

import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

        // Validate the identifier
        // request.validateId(identifier);

        BaseReview reviewToSave = request.buildReview();
        DRSKey barcodeKey = DefaultDHTKeyPair.builder()
                .locationKey(Number160.createHash(reviewToSave.getIdentifier()))
                .contentKey(Number160.createHash(reviewToSave.getContent()))
                .domainKey(DHTConfig.PUBLISHED_DOMAIN).build();
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

                for (Map.Entry<Number640, Data> results : payload().entrySet()) {
//                    allReviews.add(results.getValue().object());
                }

                return 0;
            }
        });
    }


    @GET
    @Path("/ping")
    public String pong() {
        return "pong";
    }
}
