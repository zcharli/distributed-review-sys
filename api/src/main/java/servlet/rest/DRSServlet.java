package servlet.rest;


import review.BaseReview;
import review.CommodityReview;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by czl on 19/09/16.
 */

@Path("/drs")
public class DRSServlet {

    public DRSServlet() {
    }

    @GET
    @Path("/new")
    @Produces(MediaType.APPLICATION_JSON)
    public BaseReview get() {
        return new CommodityReview();
    }

    @GET
    @Path("/ping")
    public String pong() {
        return "pong";
    }

}
