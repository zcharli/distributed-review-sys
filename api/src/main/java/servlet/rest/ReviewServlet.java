package servlet.rest;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import config.DHTConfig;
import core.DHTManager;
import core.GlobalContext;
import error.GenericReply;
import key.DRSKey;
import key.DefaultDHTKeyPair;
import msg.AsyncComplete;
import msg.AsyncResult;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.storage.Data;
import org.apache.commons.lang3.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import review.BaseReview;
import review.ReviewIdentity;
import review.comparator.ReviewTimestampComparator;
import review.request.LimitQueryParam;
import review.response.OperationCompleteResponse;
import review.response.ReviewGetResponse;
import validator.ExternalReview;

import javax.script.*;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.FileReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by czl on 04/10/16.
 */
@Path("/review")
public class ReviewServlet {
    private final static Logger LOGGER = LoggerFactory.getLogger(ReviewServlet.class);
    private final ExecutorService m_queryWorker = Executors.newFixedThreadPool(4);

    public static final String renderScript = ("{dust.render(name, " +
            "JSON.parse(json), "
            + "function(err,data) { "
            + "if(err) { "
            + "throw new Error(err);"
            + "} "
            + "else { "
            + "writer.write( data, 0, data.length );"
            + "}  "
            + "});}");

    public ReviewServlet() {
    }

    @PUT
    @Path("/new/{m_productName}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void createNewReview(final @ExternalReview BaseReview request,
                                final @Suspended AsyncResponse response,
                                final @PathParam("m_productName") String identifier) {
        String productName = null;
        try {
            productName = WordUtils.capitalize(URLDecoder.decode(identifier, "utf-8"));
            if (productName == null) {
                throw new NullPointerException("URL decoder failed");
            }
        } catch (Exception e) {
            response.resume(Response.status(Response.Status.BAD_REQUEST).entity(new GenericReply<String>("500", "Malformed request parameter, please use utf-8 encoding.")));
        }
        request.m_productName = productName;
        Number160 locationKey = Number160.createHash(request.getIdentifier());
        Number160 newDomainKey = DHTConfig.ACCEPTANCE_DOMAIN;
        Number160 contentKey = Number160.createHash(request.getContent());
        Number640 reviewKey = new Number640(locationKey, newDomainKey, contentKey, Number160.ZERO);

        request.fillInIds(locationKey, contentKey, newDomainKey, reviewKey);
        // TODO: Validate the identifier
        // request.validateId(identifier);
        DRSKey barcodeKey = DefaultDHTKeyPair.builder()
                .locationKey(locationKey)
                .contentKey(contentKey)
                .domainKey(DHTConfig.ACCEPTANCE_DOMAIN).build();
        putReview(barcodeKey, request, response);
    }

    @PUT
    @Path("/upvote/{locationId}/{contentId}")
    @Produces(MediaType.APPLICATION_JSON)
    public void upvoteReview(final @Suspended AsyncResponse response,
                             final @PathParam("locationId") String locationId,
                             final @PathParam("contentId") String contentId) {
        if (Strings.isNullOrEmpty(locationId) || Strings.isNullOrEmpty(contentId)) {
            response.resume(Response.status(Response.Status.BAD_REQUEST).entity(new GenericReply<String>("400", "Missing essential identifiers")));
            return;
        }

        try {
            final Number160 location = new Number160(locationId);
            final Number160 content = new Number160(contentId);

            final DRSKey reviewKey = DefaultDHTKeyPair.builder().contentKey(content)
                    .domainKey(DHTConfig.PUBLISHED_DOMAIN).locationKey(location).build();
            DHTManager.instance().getAllFromStorage(reviewKey, new AsyncResult() {
                @Override
                public Integer call() throws Exception {
                    if (!isSuccessful()) {
                        response.resume(Response.serverError()
                                .entity(new GenericReply<String>(
                                        "DHT-GET", "An error occurred when trying to handle the upload request"))
                                .build());
                        return 0;
                    }
                    if (payload().size() > 0) {
                        BaseReview review = null;
                        for (Map.Entry<Number640, Data> entry : payload().entrySet()) {
                            if (entry.getKey().contentKey().equals(content)) {
                                review = ((ReviewIdentity) entry.getValue().object()).identity();
                                review.m_upvotes++;
                                putReview(reviewKey, review, response);
                                break;
                            }
                        }
                        if (review == null) {
                            response.resume(Response.status(Response.Status.NOT_FOUND)
                                    .entity(new GenericReply<String>("500", "Could not find the respective review to upvote")).build());
                        }
                    } else {
                        response.resume(Response.ok(new GenericReply<String>("500", "Could not find the respective review to update")).build());
                    }
                    return 0;
                }
            });
        } catch (Exception e) {
            response.resume(Response.status(Response.Status.BAD_REQUEST).entity(new GenericReply<String>("400", "Malformatted identifiers")));
            return;
        }
    }

    @GET
    @Path("/embed/{barcode}")
    @Produces(MediaType.TEXT_HTML)
    public void getReviewIFrame(final @Suspended AsyncResponse response,
                                final @PathParam("barcode") String barcode) {
        if (Strings.isNullOrEmpty(barcode)) {
            response.resume(Response.status(Response.Status.BAD_REQUEST).entity("Missing barcode parameter").build());
            return;
        }
        ScriptEngineManager engineManager = new ScriptEngineManager();
        ScriptEngine engine = engineManager.getEngineByName("nashorn");

        String dustJSPath = this.getClass().getClassLoader().getResource("embed/dust-full.min.js").getPath();
        try {
            engine.eval(new FileReader(dustJSPath));
        } catch (Exception e) {
            LOGGER.error("Dust JS path could not be reached by file reader");
            response.resume(Response.serverError().entity("Oops and error occurred").build());
            return;
        }
        Invocable invocable = (Invocable) engine;
        Object dustjs = null;
        try {
            dustjs = engine.eval("dust");
        } catch (Exception e) {
            LOGGER.error("Dust JS evaluation error.");
            response.resume(Response.serverError().entity("Oops and error occurred during eval").build());
            return;
        }

        String embedTemplatePath = this.getClass().getClassLoader().getResource("embed/embed.dust").getPath();
        String dustTemplate = "";
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(embedTemplatePath));
            dustTemplate = new String(encoded, StandardCharsets.UTF_8);
        } catch (Exception e) {
            LOGGER.error("Embed path could not be reached by file reader");
            response.resume(Response.serverError().entity("Oops and error occurred").build());
            return;
        }

        Object compileTemplate = null;
        try {
            compileTemplate = invocable.invokeMethod(dustjs, "compile", dustTemplate, "embededDrs");
        } catch (Exception e) {
            LOGGER.error("Compilation failed to comple dust template");
            response.resume(Response.serverError().entity("Oops and error occurred during template Compilation").build());
            return;
        }
        Object loadedSource = null;

        try {
            loadedSource = invocable.invokeMethod(dustjs, "loadSource", compileTemplate);
        } catch (Exception e) {
            LOGGER.error("Compilation failed to loadSource on dust template");
            response.resume(Response.serverError().entity("Oops and error occurred during template loading").build());
            return;
        }
        getReviewByBarcode(barcode, new AsyncResult() {
            @Override
            public Integer call() throws Exception {
                if (!isSuccessful() || payload() == null) {
                    response.resume(Response.serverError()
                            .entity(new GenericReply<String>(
                                    "DHT-GET", "An error occurred when trying to get id " + barcode))
                            .build());
                    return 0;
                }

                Writer writer = new StringWriter();



                Bindings bindings = new SimpleBindings();

                Set<Map.Entry<Number640, Data>> allResults = payload().entrySet();
                if (allResults.size() == 0) {
                    String noResultsJson = "{\"results\": \"[]\"}";

                    bindings.put("name", "embededDrs");
                    bindings.put("json", noResultsJson);
                    bindings.put("writer", writer);
                    engine.getContext().setBindings(bindings, ScriptContext.GLOBAL_SCOPE);
                    System.out.println(writer);
                    response.resume(Response.ok(new OperationCompleteResponse<String>("404", "No results were found.")).build());
                    return 0;
                }



                try {
                    engine.eval(renderScript, engine.getContext());
                } catch (Exception e) {
                    LOGGER.error("Compilation failed during execution");
                    response.resume(Response.serverError().entity("Oops and error occurred during template execution").build());
                    return 0;
                }

                List<BaseReview> allReviews = new ArrayList<BaseReview>();
                List<BaseReview> limitedReviews = new LinkedList<BaseReview>();

                for (Map.Entry<Number640, Data> results : allResults) {
                    allReviews.add(((ReviewIdentity) (results.getValue().object())).identity());
                }

                Collections.sort(allReviews, new ReviewTimestampComparator());

                int max = limit.page * limit.step;
                int start = (limit.page - 1) * limit.step;
                if (max > allReviews.size()) {
                    max = allReviews.size();
                }
                for (int i = start; i < max; i++) {
                    limitedReviews.add(allReviews.get(i));
                }
                response.resume(Response.ok(new ReviewGetResponse(200, limitedReviews)).build());
                return 0;
            }
        });


        System.out.println(writer);
        response.resume(Response.status(Response.Status.BAD_REQUEST).entity("Hello world").build());
    }

    @PUT
    @Path("/downvote/{locationId}/{contentId}")
    @Produces(MediaType.APPLICATION_JSON)
    public void downvoteReview(final @Suspended AsyncResponse response,
                               final @PathParam("locationId") String locationId,
                               final @PathParam("contentId") String contentId) {
        if (Strings.isNullOrEmpty(locationId) || Strings.isNullOrEmpty(contentId)) {
            response.resume(Response.status(Response.Status.BAD_REQUEST).entity(new GenericReply<String>("400", "Missing essential identifiers")));
            return;
        }

        try {
            final Number160 location = new Number160(locationId);
            final Number160 content = new Number160(contentId);

            final DRSKey reviewKey = DefaultDHTKeyPair.builder().contentKey(content)
                    .domainKey(DHTConfig.PUBLISHED_DOMAIN).locationKey(location).build();
            DHTManager.instance().getAllFromStorage(reviewKey, new AsyncResult() {
                @Override
                public Integer call() throws Exception {
                    if (!isSuccessful()) {
                        response.resume(Response.serverError()
                                .entity(new GenericReply<String>(
                                        "DHT-GET", "An error occurred when trying to handle the upload request"))
                                .build());
                        return 0;
                    }
                    if (payload().size() > 0) {
                        BaseReview review = null;
                        for (Map.Entry<Number640, Data> entry : payload().entrySet()) {
                            if (entry.getKey().contentKey().equals(content)) {
                                review = ((ReviewIdentity) entry.getValue().object()).identity();
                                review.m_downvotes++;
                                putReview(reviewKey, review, response);
                                break;
                            }
                        }
                        if (review == null) {
                            response.resume(Response.status(Response.Status.NOT_FOUND)
                                    .entity(new GenericReply<String>("500", "Could not find the respective review to upvote")).build());
                        }
                    } else {
                        response.resume(Response.ok(new GenericReply<String>("500", "Could not find the respective review to update")).build());
                    }
                    return 0;
                }
            });
        } catch (Exception e) {
            response.resume(Response.status(Response.Status.BAD_REQUEST).entity(new GenericReply<String>("400", "Malformatted identifiers")));
            return;
        }
    }

    @PUT
    @Path("/update/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void updateReview(final @ExternalReview BaseReview request,
                             final @Suspended AsyncResponse response,
                             final @PathParam("id") String identifier) {
        if (!identifier.equals(request.getIdentifier()) || request.m_contentId == null || request.m_locationId == null || request.m_domainId == null) {
            response.resume(Response.status(Response.Status.BAD_REQUEST).entity(new GenericReply<String>("500", "Invalid request parameters")));
            return;
        }

        // TODO: Validate the identifier and the key to make sure it even exists first
        // request.validateId(identifier);
        DRSKey barcodeKey = DefaultDHTKeyPair.builder()
                .locationKey(Number160.createHash(request.getIdentifier()))
                .contentKey(Number160.createHash(request.getContent()))
                .domainKey(DHTConfig.PUBLISHED_DOMAIN).build();

        DHTManager.instance().getAllFromStorage(barcodeKey, new AsyncResult() {
            @Override
            public Integer call() throws Exception {

                if (!isSuccessful()) {
                    response.resume(Response.serverError()
                            .entity(new GenericReply<String>(
                                    "DHT-GET", "An error occurred when trying to get id " + identifier))
                            .build());
                    return 0;
                }

                if (payload().size() > 0) {
                    putReview(barcodeKey, request, response);
                } else {
                    response.resume(Response.ok(new GenericReply<String>("500", "Could not find the respective review to update")).build());
                }

                return 0;
            }
        });
    }

    @PUT
    @Consumes({MediaType.APPLICATION_JSON, "application/vnd.api+json"})
    @Produces({MediaType.APPLICATION_JSON, "application/vnd.api+json"})
    @Path("accept/{identifier}")
    public void acceptReviewIntoPublished(final @ExternalReview BaseReview request,
                                          final @Suspended AsyncResponse response,
                                          final @PathParam("identifier") String identifier) {
        if (Strings.isNullOrEmpty(identifier)) {
            response.resume(Response.status(Response.Status.BAD_REQUEST).entity(new GenericReply<String>("404", "No identifier in request was found")));
            return;
        }

        // TODO: handle fail case where identifier has already been approved or does not exist, atm it will never end cause of this
        final Number160 locationKey = Number160.createHash(request.getIdentifier());
        final Number160 newDomainKey = DHTConfig.ACCEPTANCE_DOMAIN;
        final Number160 contentKey = new Number160(request.getContentId());
        final DRSKey reviewKey = DefaultDHTKeyPair.builder()
                .locationKey(locationKey)
                .contentKey(contentKey)
                .domainKey(newDomainKey)
                .build();
        // The original review is updated since the only time editing is allowed is during acceptance.
        Number640 fullKey = new Number640(locationKey, DHTConfig.PUBLISHED_DOMAIN, Number160.createHash(request.getContent()), Number160.ZERO);
        request.fillInIds(locationKey, contentKey, newDomainKey, fullKey);

        final DRSKey publishedKey = DefaultDHTKeyPair.builder().contentKey(fullKey.contentKey())
                .domainKey(DHTConfig.PUBLISHED_DOMAIN).locationKey(fullKey.locationKey()).build();
        request.m_publishTime = System.currentTimeMillis();
        DHTManager.instance().approveData(reviewKey, publishedKey, request, new AsyncComplete() {
            @Override
            public Integer call() {
                if (!isSuccessful()) {
                    response.resume(Response.serverError().entity(
                            new GenericReply<String>("DHT-ACCEPT", message())
                    ).build());
                } else {
                    request.fillInIds(publishedKey.getLocationKey(), publishedKey.getLocationKey(), DHTConfig.PUBLISHED_DOMAIN, fullKey);
                    response.resume(Response.ok().entity(
                            new OperationCompleteResponse<BaseReview>("200", request)
                    ).build());
                }
                return 0;
            }
        });
    }

    @PUT
    @Consumes({MediaType.APPLICATION_JSON, "application/vnd.api+json"})
    @Produces({MediaType.APPLICATION_JSON, "application/vnd.api+json"})
    @Path("deny/{identifier}")
    public void denyReviewFromAcceptance(final @ExternalReview BaseReview request,
                                         final @Suspended AsyncResponse response,
                                         final @PathParam("identifier") String identifier) {
        if (Strings.isNullOrEmpty(identifier)) {
            response.resume(Response.status(Response.Status.BAD_REQUEST).entity(new GenericReply<String>("404", "No identifier in request was found")));
            return;
        }
        Number160 locationKey = Number160.createHash(identifier);
        Number160 newDomainKey = DHTConfig.ACCEPTANCE_DOMAIN;
        Number160 contentKey = new Number160(request.getContentId());
        DRSKey reviewKey = DefaultDHTKeyPair.builder()
                .locationKey(locationKey)
                .contentKey(contentKey)
                .domainKey(newDomainKey)
                .build();
        DHTManager.instance().removeFromStorage(reviewKey, new AsyncComplete() {
            @Override
            public Integer call() {
                if (!isSuccessful()) {
                    response.resume(Response.serverError().entity(
                            new GenericReply<String>("DHT-ACCEPT", message())
                    ).build());
                } else {
                    response.resume(Response.ok().entity(
                            new OperationCompleteResponse<String>("200", "Success")
                    ).build());
                }
                return 0;
            }
        });
    }

    @GET
    @Path("get/{identifier}")
    @Produces(MediaType.APPLICATION_JSON)
    public void getReview(final @PathParam("identifier") String identifier,
                          final @BeanParam LimitQueryParam limit,
                          final @Suspended AsyncResponse response) {
        if (Strings.isNullOrEmpty(identifier)) {

            response.resume(Response.status(Response.Status.BAD_REQUEST)
                    .entity(new GenericReply<String>(
                            "400", "Request is missing barcode/identifier"))
                    .build());
            return;
        }
        getReviewByBarcode(identifier, new AsyncResult() {
            @Override
            public Integer call() throws Exception {

                if (!isSuccessful() || payload() == null) {
                    response.resume(Response.serverError()
                            .entity(new GenericReply<String>(
                                    "DHT-GET", "An error occurred when trying to get id " + identifier))
                            .build());
                    return 0;
                }
                Set<Map.Entry<Number640, Data>> allResults = payload().entrySet();
                if (allResults.size() == 0) {
                    response.resume(Response.ok(new OperationCompleteResponse<String>("404", "No results were found.")).build());
                    return 0;
                }

                List<BaseReview> allReviews = new ArrayList<BaseReview>();
                List<BaseReview> limitedReviews = new LinkedList<BaseReview>();

                for (Map.Entry<Number640, Data> results : allResults) {
                    allReviews.add(((ReviewIdentity) (results.getValue().object())).identity());
                }

                Collections.sort(allReviews, new ReviewTimestampComparator());

                int max = limit.page * limit.step;
                int start = (limit.page - 1) * limit.step;
                if (max > allReviews.size()) {
                    max = allReviews.size();
                }
                for (int i = start; i < max; i++) {
                    limitedReviews.add(allReviews.get(i));
                }
                response.resume(Response.ok(new ReviewGetResponse(200, limitedReviews)).build());
                return 0;
            }
        });

    }

    @GET
    @Consumes({MediaType.APPLICATION_JSON, "application/vnd.api+json"})
    @Produces({MediaType.APPLICATION_JSON, "application/vnd.api+json"})
    @Path("approval")
    public void getTrackedReviewsNeedingApproval(final @Suspended AsyncResponse response) {
        ImmutableList<Number160> trackedIds = DHTManager.instance().getTrackedFromAcceptanceDomain();
        if (trackedIds == null) {
            response.resume(Response.serverError().entity(new GenericReply<String>("DHT-APPROVAL", "Unabled to find tracked IDs on this node.")));
        }
//        List<BaseReview> results = new LinkedList<>();
//        for (Number160 locationId : trackedIds) {
//            DRSKey instanceKey = DefaultDHTKeyPair.builder().locationKey(locationId).domainKey(DHTConfig.ACCEPTANCE_DOMAIN).build();
//            for (Data result : DHTManager.instance().getAllFromStorage(instanceKey)) {
//                try {
//                    results.add(((ReviewIdentity)result.object()).identity());
//                } catch (Exception e) {
//                    response.resume(Response.serverError().entity(
//                            new GenericReply<String>("DHT-APPROVAL", "Failed to extract data from DHT.")
//                    ).build());
//                }
//            }
//        }
//        response.resume(Response.ok().entity(new OperationCompleteResponse<List<BaseReview>>("200",results)).build());
        final List<BaseReview> reviewsInAcceptance = new LinkedList<>();
        final CompletableFuture<?>[] allAcceptanceReviews = trackedIds.stream()
                .map(key -> CompletableFuture.runAsync(() -> {
                    final Collection<Data> reviews = DHTManager.instance()
                            .getAllFromStorage(DefaultDHTKeyPair.builder()
                                    .locationKey(key)
                                    .domainKey(DHTConfig.ACCEPTANCE_DOMAIN)
                                    .build());
                    if (reviews != null) {
                        reviews.forEach(review -> {
                            try {
                                final BaseReview basePointer = (BaseReview) review.object();
                                reviewsInAcceptance.add(basePointer);
                            } catch (Exception e) {
                                LOGGER.error("Exception when trying to retrieve acceptance review object from Data: " + e.getMessage());
                            }
                        });
                    }
                }, m_queryWorker).exceptionally(ex -> {
                    LOGGER.error("An error occured when fetch all acceptance reviews from locations: " + ex.getMessage());
                    ex.printStackTrace();
                    return null;
                }))
                .toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(allAcceptanceReviews).join();
        response.resume(Response.ok().entity(new ReviewGetResponse(200, reviewsInAcceptance)).build());
        GlobalContext.instance().invalidateCache();
    }

    @GET
    @Path("/ping")
    public String pong() {
        return "pong";
    }

    private void putReview(DRSKey barcodeKey, BaseReview request, AsyncResponse response) {
        DHTManager.instance().putContentOnStorage(barcodeKey, request, new AsyncComplete() {
            @Override
            public Integer call() {
                if (!isSuccessful()) {
                    response.resume(Response.serverError()
                            .entity(new GenericReply<String>(
                                    "DHT-PUT", "An error occurred when trying to put object into the DHT and thus has failed"))
                            .build());
                } else {
                    response.resume(Response.ok(new OperationCompleteResponse<>("DHT-PUT", "Success")).build());
                }
                return 0;
            }
        });
    }

    private void getReviewByBarcode(final String identifier, final AsyncResult callback) {
        DRSKey reviewKey = DefaultDHTKeyPair.builder()
                .locationKey(Number160.createHash(identifier))
                .domainKey(DHTConfig.PUBLISHED_DOMAIN)
                .build();
        DHTManager.instance().getAllFromStorage(reviewKey, callback);
    }
}
