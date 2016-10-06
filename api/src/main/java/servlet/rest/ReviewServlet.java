package servlet.rest;

import com.google.common.collect.ImmutableMap;
import error.GenericError;
import exception.ValidationExceptionMapper;
import key.DRSKey;
import key.DefaultDHTKeyPair;
import net.tomp2p.peers.Number160;
import request.CreateReviewRequest;
import review.BaseReview;
import review.ReviewTypeFactory;
import validator.ExternalReview;

import javax.validation.Valid;
import javax.validation.ValidationException;
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

    private static final ImmutableMap<String, ReviewTypeFactory> m_validTypes =
            ImmutableMap.of(
                    "commodity", ReviewTypeFactory.COMMODITY_REVIEW,
                    "restaurant", ReviewTypeFactory.RESTAURANT_REVIEW);

    @POST
    @Path("/{type}/new")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void createNewReview(final @ExternalReview CreateReviewRequest request, @PathParam("type") String type,
                                final @Suspended AsyncResponse response) throws ValidationException {
        if (type == null || !m_validTypes.containsKey(type.toLowerCase())) {
            response.resume(Response.status(Response.Status.BAD_REQUEST).entity(new GenericError<String>("412", "Invalid review type.")).build());
            return;
        }

        if (!request.isValid()) {
            response.resume(Response.status(Response.Status.BAD_REQUEST).entity(new GenericError<String>("412","Parameter error on request.")).build());
            return;
        }

        new Thread() {
            @Override
            public void run() {

                ReviewTypeFactory factory = m_validTypes.get(type);
                BaseReview reviewObject = factory.manufacture(request);

                DRSKey barcodeKey = DefaultDHTKeyPair.builder()
                        .locationKey(Number160.createHash(request.identifier))
                        .contentKey(Number160.createHash(request.review)).build();
                //DHTManager.instance().addToStorage(barcodeKey, new AsyncComplete());
            }
        };
    }

    @GET
    @Path("/ping")
    public String pong() {
        return "pong";
    }
}
