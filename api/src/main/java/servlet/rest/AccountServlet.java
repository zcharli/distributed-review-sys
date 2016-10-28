package servlet.rest;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import config.APIConfig;
import config.DHTConfig;
import error.GenericReply;
import net.tomp2p.peers.Number160;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import review.request.CreateAccountRequest;
import review.request.LoginRequest;
import review.response.LoginResponse;
import review.response.OperationCompleteResponse;
import user.BaseAccount;
import validator.ExternalAccount;
import validator.ExternalCreateAccount;
import validator.ExternalLogin;
import validator.PasswordAuthentication;

import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.Random;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by czl on 19/09/16.
 */

@Path("/account")
public class AccountServlet {
    private final static Logger LOGGER = LoggerFactory.getLogger(AccountServlet.class);
    private static final String IMAGE_PATTERN = "([^\\s]+(\\.(?i)(jpg|png|gif|bmp))$)";

    private Pattern pattern;
    private Matcher matcher;

    private Random random = new Random();
    ExecutorService executor = Executors.newSingleThreadExecutor();
    private ObjectMapper objectMapper = new ObjectMapper();

    public AccountServlet() {
        pattern = Pattern.compile(IMAGE_PATTERN);
    }

    @PUT
    @Path("/update")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateAccountInfo(final @ExternalAccount BaseAccount request) {
        Future<String> hashedPassword = null;
        if (!Strings.isNullOrEmpty(request.m_password)) {
            hashedPassword = executor.submit(new SaltedPasswordGenThread(request.m_password));
        }
        try (Jedis adapter = DHTConfig.REDIS_RESOURCE_POOL.getResource()) {
            String accountJson = adapter.get(createUsernameKey(request.m_email));
            if (Strings.isNullOrEmpty(accountJson)) {
                return Response.noContent().entity(new GenericReply<String>("404", "User was not found.")).build();
            }
            final BaseAccount account = objectMapper.readValue(accountJson, BaseAccount.class);
            if (account == null || Strings.isNullOrEmpty(account.m_password)) {
                return Response.accepted().entity(new GenericReply<String>("404", "User was not found.")).build();
            }
            if (!account.hasToken(request.m_loginToken)) {
                return Response.accepted().entity(new GenericReply<String>("404", "There was an error while processing your last request, please signout and sign in again.")).build();
            }
            if (!account.m_password.equals(request.m_password) && hashedPassword != null) {
                account.m_password = hashedPassword.get();
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
            if (Strings.isNullOrEmpty(accountJson)) {
                return Response.noContent().entity(new GenericReply<String>("404", "User was not found.")).build();
            }
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
                return Response.accepted().entity(new LoginResponse<BaseAccount>(200, account)).build();
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

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadFile(@FormDataParam("file") InputStream uploadedInputStream,
                               @FormDataParam("file") FormDataContentDisposition fileDetail,
                               final @QueryParam("user") String userName) {
        if (Strings.isNullOrEmpty(userName)) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new GenericReply<String>("400", "Missing user identification")).build();
        }
        try (Jedis adapter = DHTConfig.REDIS_RESOURCE_POOL.getResource()) {
            String userIdentifier = createUsernameKey(userName);
            String userJson = adapter.get(userIdentifier);
            if (Strings.isNullOrEmpty(userJson)) {
                return Response.status(400).entity(new GenericReply<>("500", "There was no user by the username " + userName)).build();
            }
            if (Strings.isNullOrEmpty(APIConfig.IMAGE_UPLOAD_LOCATION)) {
                APIConfig.IMAGE_UPLOAD_LOCATION = getClass().getResource("/webapp/images").getPath(); // TODO: remove!
            }
            matcher = pattern.matcher(fileDetail.getFileName());
            if (!matcher.matches()) {
                return Response.status(Response.Status.BAD_REQUEST).entity(new GenericReply<String>("400", "Not a valid image file type")).build();
            }
            String extension = Files.getFileExtension(fileDetail.getFileName());
            String fileName = Number160.createHash(userName) + "." + extension;
            String uploadedFileLocation = APIConfig.IMAGE_UPLOAD_LOCATION + "/" + fileName;
            String resourcePath = APIConfig.ABSOLUTE_IMAGE_RESOURCE_PATH + "/" + fileName;
            try {
                writeToFile(uploadedInputStream, uploadedFileLocation);
                BaseAccount userAccount = objectMapper.readValue(userJson, BaseAccount.class);
                userAccount.m_profilePicUrl = resourcePath;
                String newAccountJson = objectMapper.writeValueAsString(userAccount);
                String result = adapter.set(userIdentifier, newAccountJson);
                if (!result.equals("OK")) {
                    throw new IOException("Failed to write new user info to disk");
                }
                return Response.status(200).entity(new OperationCompleteResponse<String>("200", resourcePath)).build();
            } catch (Exception e) {
                return Response.status(500).entity(new GenericReply<>("500", "Server error while writing upload to disk: " + e.getMessage())).build();
            }
        }
    }

    // save uploaded file to new location
    private void writeToFile(InputStream uploadedInputStream, String uploadedFileLocation) throws IOException {
            OutputStream out = new FileOutputStream(new File(uploadedFileLocation));
            int read = 0;
            byte[] bytes = new byte[1024];
            out = new FileOutputStream(new File(uploadedFileLocation));
            while ((read = uploadedInputStream.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            out.flush();
            out.close();
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
