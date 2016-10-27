package servlet.rest;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.primitives.Booleans;
import config.DHTConfig;
import error.GenericReply;
import redis.clients.jedis.Jedis;
import review.request.CreateAccountRequest;
import review.request.LoginRequest;
import review.response.LoginResponse;
import review.response.ReviewOperationComplete;
import user.BaseAccount;
import validator.ExternalCreateAccount;
import validator.ExternalLogin;
import validator.PasswordAuthentication;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
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
    ExecutorService executor = Executors.newSingleThreadExecutor();
    private ObjectMapper objectMapper = new ObjectMapper();
    public AccountServlet() { }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response loginUser(final @ExternalLogin LoginRequest request) {
        BaseAccount account = null;
        try (Jedis adapter = DHTConfig.REDIS_RESOURCE_POOL.getResource()) {
            String accountJson = adapter.get(createUsernameKey(request.username));
            account = objectMapper.readValue(accountJson, BaseAccount.class);
            if (Strings.isNullOrEmpty(account.m_password)) {
                return Response.noContent().entity(new GenericReply<String>("404", "User was not found.")).build();
            } else {
                if (!PasswordAuthentication.instance().authenticate(request.password.toCharArray(), account.m_password)) {
                    return Response.accepted().entity(new GenericReply<String>("404", "Password or username is incorrect.")).build();
                }
             }
        } catch (Exception e) {
            return Response.serverError().entity(new GenericReply<String>("500", "An error occurred during the request: " + e.getMessage())).build();
        }
        return Response.accepted().entity(new LoginResponse<BaseAccount>(200, account)).build();
    }

    @POST
    @Path("/new")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createAccount(final @ExternalCreateAccount CreateAccountRequest request) {
        // Maybe this isn't as fast as I thought...
        Future<String> hashedPassword = executor.submit(new SaltedPasswordGenThread(request.password));
        try (Jedis adapter = DHTConfig.REDIS_RESOURCE_POOL.getResource()) {
            String accountExistance = adapter.get(createUsernameKey(request.identification));
            if (!Strings.isNullOrEmpty(accountExistance)) {
                return Response.status(Response.Status.CONFLICT).entity(new GenericReply<String>("400", "A user has already registered that email.")).build();
            }
            String saltyPassword = hashedPassword.get();
            BaseAccount account = new BaseAccount(request.identification, saltyPassword);
            String accountJson = objectMapper.writeValueAsString(account);
            String reply = adapter.set(createUsernameKey(request.identification), accountJson);
            if (reply.equals("OK")) {
                return Response.accepted().entity(new LoginResponse(200, account)).build();
            } else {
                return Response.serverError().entity(new GenericReply<String>("500", "Account creation has failed.")).build();
            }
        } catch (Exception e) {
            return Response.serverError().entity(new GenericReply<String>("500", "An persistence error occurred during the request: " + e.getMessage())).build();
        }
    }

    @GET
    @Consumes({MediaType.APPLICATION_JSON, "application/vnd.api+json"})
    @Produces({MediaType.APPLICATION_JSON, "application/vnd.api+json"})
    @Path("/fetch")
    public void getAccountInfo(final @Suspended AsyncResponse response,
                               final @QueryParam("email") String email) {
        if (Strings.isNullOrEmpty(email)) {
            response.resume(Response.status(Response.Status.BAD_REQUEST).entity(new GenericReply<String>("400", "Missing email parameter for user query")).build());
        }
        BaseAccount account = null;
        try (Jedis adapter = DHTConfig.REDIS_RESOURCE_POOL.getResource()) {
            String accountJson = adapter.get(createUsernameKey(email));
            if (Strings.isNullOrEmpty(accountJson)) {
                response.resume(Response.noContent().entity(new GenericReply<String>("404", "User was not found.")).build());
            } else {
                account = objectMapper.readValue(accountJson, BaseAccount.class);
                if (account == null || Strings.isNullOrEmpty(account.m_password)) {
                    response.resume(Response.noContent().entity(new GenericReply<String>("404", "User was not found.")).build());
                } else {
                    response.resume(Response.accepted().entity(new LoginResponse<BaseAccount>(200, account)).build());
                }
            }
        } catch (Exception e) {
            response.resume(Response.serverError().entity(new GenericReply<String>("500", "An error occurred during the request: " + e.getMessage())).build());
        }
    }

    @GET
    @Path("/access")
    @Produces(MediaType.APPLICATION_JSON)
    public void getAccountAccessInfo() {

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
