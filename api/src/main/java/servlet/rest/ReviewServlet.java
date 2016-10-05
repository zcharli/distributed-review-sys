package servlet.rest;

import com.google.common.collect.ImmutableSet;
import request.CreateReviewRequest;

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

    private static final ImmutableSet<String> m_validTypes = ImmutableSet.of("commodity", "restaurant");

    @POST
    @Path("/{type}/new")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void createNewReview(final CreateReviewRequest request, @PathParam("type") String type,
                                final @Suspended AsyncResponse response) {
        if (type == null || !m_validTypes.contains(type.toLowerCase())) {
            response.resume(Response.status(Response.Status.BAD_REQUEST).entity("Invalid review type.").build());
            return;
        }

        new Thread() {
            @Override
            public void run() {

            }
        };
    }
}
