package servlet.rest;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.primitives.Booleans;
import config.DHTConfig;
import error.GenericReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import review.request.CreateAccountRequest;
import review.request.LoginRequest;
import review.response.LoginResponse;
import review.response.ReviewOperationComplete;
import user.BaseAccount;
import validator.ExternalAccount;
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
import java.util.concurrent.*;

/**
 * Created by czl on 19/09/16.
 */

@Path("/account")
public class AccountServlet {
    private final static Logger LOGGER = LoggerFactory.getLogger(AccountServlet.class);

    private Random random = new Random();
    ExecutorService executor = Executors.newSingleThreadExecutor();
    private ObjectMapper objectMapper = new ObjectMapper();

    public AccountServlet() {
    }

    @PUT
    @Path("/update")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateAccountInfo(final @ExternalAccount BaseAccount request) {
        try (Jedis adapter = DHTConfig.REDIS_RESOURCE_POOL.getResource()) {
            String accountJson = adapter.get(createUsernameKey(request.m_email));
            final BaseAccount account = objectMapper.readValue(accountJson, BaseAccount.class);
            if (account == null || Strings.isNullOrEmpty(account.m_password)) {
                return Response.accepted().entity(new GenericReply<String>("404", "User was not found.")).build();
            } else {
                if (!account.hasToken(request.m_loginToken)) {
                    return Response.accepted().entity(new GenericReply<String>("404", "There was an error while processing your last request, please signout and sign in again.")).build();
                }
            }
            account.addToken(random.nextLong());
            CompletableFuture saveUserFuture = CompletableFuture.runAsync(() -> {
                if (saveAccount(account, adapter)) {
                    LOGGER.debug("Save account successful during saveUserFuture");
                }else {
                    LOGGER.error("Error during saveUserFuture, user was not saved");
                }
            }, executor).exceptionally(ex -> {
                LOGGER.error("There was an error saving user future in updateAccount: " + ex.getMessage());
                ex.printStackTrace();
                return null;
            });
            return Response.accepted().entity(new LoginResponse<BaseAccount>(200, account)).build();
        } catch (Exception e) {
            return Response.serverError().entity(new GenericReply<String>("500", "An error occurred during the request: " + e.getMessage())).build();
        }
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response loginUser(final @ExternalLogin LoginRequest request) {
        try (Jedis adapter = DHTConfig.REDIS_RESOURCE_POOL.getResource()) {
            String accountJson = adapter.get(createUsernameKey(request.username));
            final BaseAccount account = objectMapper.readValue(accountJson, BaseAccount.class);
            if (account == null || Strings.isNullOrEmpty(account.m_password)) {
                return Response.noContent().entity(new GenericReply<String>("404", "User was not found.")).build();
            } else {
                if (!PasswordAuthentication.instance().authenticate(request.password.toCharArray(), account.m_password)) {
                    return Response.accepted().entity(new GenericReply<String>("404", "Password or username is incorrect.")).build();
                }
            }
            account.addToken(random.nextLong());
            CompletableFuture saveUserFuture = CompletableFuture.runAsync(() -> {
                saveAccount(account, adapter);
            }, executor);
            return Response.accepted().entity(new LoginResponse<BaseAccount>(200, account)).build();
        } catch (Exception e) {
            return Response.serverError().entity(new GenericReply<String>("500", "An error occurred during the request: " + e.getMessage())).build();
        }
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
            if (saveAccount(account, adapter)) {
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

    private boolean saveAccount(BaseAccount user, Jedis adapter) {
        try {
            String accountJson = objectMapper.writeValueAsString(user);
            String reply = adapter.set(createUsernameKey(user.m_email), accountJson);
            if (!reply.equals("OK")) {
                return false;
            }
        } catch (Exception e) {
            LOGGER.error("There was a json mapper exception error when saving new account: " + e.getMessage());
        }
        return true;
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
