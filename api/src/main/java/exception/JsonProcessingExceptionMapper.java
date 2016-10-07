package exception;

import com.fasterxml.jackson.core.JsonParseException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * Created by cli on 10/7/2016.
 */
public class JsonProcessingExceptionMapper implements ExceptionMapper<JsonParseException> {
    @Context
    HttpServletRequest request;

    @Override
    public Response toResponse(JsonParseException e) {
        if (e instanceof JsonParseException) {
            String[] error =  e.getMessage().split(":");
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode()).type(MediaType.APPLICATION_JSON)
                    .entity(new ValidationError("Invalid Json post object.", request.getRequestURI(), error != null && error.length > 1 ? error[0] : "see request")).build();
        }
        return Response.serverError().entity(e.getMessage()).build();
    }
}
