package servlet.rest;


import com.google.common.base.Strings;
import com.google.common.primitives.Booleans;
import config.DHTConfig;
import error.GenericReply;
import redis.clients.jedis.Jedis;
import review.request.CreateAccountRequest;
import review.request.LoginRequest;
import review.response.LoginResponse;
import review.response.ReviewOperationComplete;
import validator.ExternalCreateAccount;
import validator.ExternalLogin;
import validator.PasswordAuthentication;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
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
    ExecutorService executor = Executors.newFixedThreadPool(10);

    public AccountServlet() { }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response loginUser(final @ExternalLogin LoginRequest request) {
        try (Jedis adapter = DHTConfig.REDIS_RESOURCE_POOL.getResource()) {
            String passwordToken = adapter.get(createUsernameKey(request.username));
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
    public Response createAccount(final @ExternalCreateAccount CreateAccountRequest request) {
        // Maybe this isn't as fast as I thought...
        Future<String> hashedPassword = executor.submit(new SaltedPasswordGenThread(request.password));
        try (Jedis adapter = DHTConfig.REDIS_RESOURCE_POOL.getResource()) {
            String saltyPassword = hashedPassword.get();
            String reply = adapter.set(createUsernameKey(request.identification), saltyPassword);
            if (reply.equals("OK")) {
                return Response.accepted().entity(new LoginResponse(200, "Success", Long.toString(random.nextLong()))).build();
            } else {
                return Response.serverError().entity(new GenericReply<String>("500", "Account creation has failed.")).build();
            }
        } catch (Exception e) {
            return Response.serverError().entity(new GenericReply<String>("500", "An persistence error occurred during the request: " + e.getMessage())).build();
        }
    }

    @GET
    @Path("/ping")
    public String pong() {
        return "pong";
    }

    private String createUsernameKey(String user) {
        return DHTConfig.REDIS_USERNAME_PREFIX + user;
    }

    public static class SaltedPasswordGenThread implements Callable<String> {
        String m_nonSaltedPassword;
        public SaltedPasswordGenThread(String password) {
            m_nonSaltedPassword = password;
        }
        @Override
        public String call() {
            return PasswordAuthentication.instance().hash(m_nonSaltedPassword.toCharArray());
        }
    }
}
