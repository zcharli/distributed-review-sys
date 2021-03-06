package exception;

/**
 * Created by cli on 10/5/2016.
 */
import error.GenericReply;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ApplicationExceptionMapper implements ExceptionMapper<Exception> {

    @Produces(MediaType.APPLICATION_JSON)
    public Response toResponse(Exception e) {
        System.out.println("Application ExceptionMapper Problem");
        return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                .type(MediaType.TEXT_PLAIN)
                .entity(new GenericReply<String>("500", e.getMessage()))
                .build();
    }
}