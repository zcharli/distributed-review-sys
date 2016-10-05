package servlet.rest;


import review.BaseReview;
import review.CommodityReview;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Created by czl on 19/09/16.
 */

@Path("/drs")
public class DRSServlet {

    public DRSServlet() {
    }

    @POST @Consumes("application/json")
    @Path("/new")
    @Produces(MediaType.APPLICATION_JSON)
    public BaseReview get() {
        return new CommodityReview();
    }
}
