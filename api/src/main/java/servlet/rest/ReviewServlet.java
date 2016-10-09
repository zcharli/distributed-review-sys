package servlet.rest;

import config.DHTConfig;
import core.DHTManager;
import error.GenericReply;
import key.DRSKey;
import key.DefaultDHTKeyPair;
import msg.AsyncComplete;
import net.tomp2p.peers.Number160;
import review.BaseReview;
import review.request.BaseCRRequest;
import validator.ExternalReview;

import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by czl on 04/10/16.
 */
@Path("/review")
public class ReviewServlet {

    public ReviewServlet() {
    }

    @POST
    @Path("/new")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void createNewReview(final @ExternalReview BaseCRRequest request,
                                final @Suspended AsyncResponse response) {
        new Thread(() -> {
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
        ).start();
    }

    @GET
    @Path("/ping")
    public String pong() {
        return "pong";
    }
}
