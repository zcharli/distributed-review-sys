package servlet.rest;


import com.google.common.base.Strings;
import config.DHTConfig;
import error.GenericReply;
import redis.clients.jedis.Jedis;
import review.request.LoginRequest;
import review.response.LoginResponse;
import validator.ExternalLogin;
import validator.PasswordAuthentication;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by czl on 19/09/16.
 */

@Path("/account")
public class AccountServlet {

    private Random random = new Random();

    public AccountServlet() {
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response loginUser(final @ExternalLogin LoginRequest request) {
        try (Jedis adapter = DHTConfig.REDIS_RESOURCE_POOL.getResource()) {
            String passwordToken = adapter.get("drs:webapp:user" + request.username);
            if (Strings.isNullOrEmpty(passwordToken)) {
                return Response.noContent().entity(new GenericReply<String>("404", "User was not found.")).build();
            } else {
                if (!PasswordAuthentication.instance().authenticate(request.password.toCharArray(), passwordToken)) {
                    return Response.accepted().entity(new GenericReply<String>("404", "Password or username is incorrect.")).build();
                }
             }
        } catch (Exception e) {
            return Response.serverError().entity(new GenericReply<String>("500", "An error occurred during the request: " + e.getMessage())).build();
        }
        return Response.accepted().entity(new LoginResponse(200, "Success", Long.toString(random.nextLong()))).build();
    }

    @POST
    @Path("/new")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createAccount() {
        return null;
    }

    @GET
    @Path("/ping")
    public String pong() {
        return "pong";
    }

}
