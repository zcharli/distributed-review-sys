package exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Created by cli on 10/7/2016.
 */
@Provider
public class JsonMappingExceptionMapper implements ExceptionMapper<JsonMappingException> {
    @Context
    HttpServletRequest request;

    @Override
    public Response toResponse(JsonMappingException e) {
        if (e instanceof InvalidTypeIdException) {
            StringBuilder sb = new StringBuilder();
            sb.append("Received invalid type ");
            sb.append(((InvalidTypeIdException) e).getTypeId());
            sb.append(" in post");
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode()).type(MediaType.APPLICATION_JSON)
                    .entity(new ValidationError(sb.toString(), request.getRequestURI(), ((InvalidTypeIdException) e).getTypeId())).build();
        }

        String[] split = e.getMessage().split(":");
        String cause = "";
        if (split.length > 2) {
            int index = split[1].indexOf('(');
            if (index > 0) {
                cause = split[1].substring(1, index - 1);
            }
        }
        return Response.status(Response.Status.BAD_REQUEST.getStatusCode()).type(MediaType.APPLICATION_JSON)
                .entity(new ValidationError("Error in request", request.getRequestURI(), cause)).build();
    }
}
