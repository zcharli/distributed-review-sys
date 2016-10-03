package servlet.webapp;

import org.eclipse.jetty.servlet.DefaultServlet;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by cli on 10/2/2016.
 */
@Path("/app")
public class DRSManagement extends DefaultServlet {

    @GET
    @Path("/")
    @Produces(MediaType.TEXT_HTML)
    public String index() {
        return "index";
    }
}