package exception;

/**
 * Created by cli on 10/5/2016.
 */
import org.glassfish.jersey.server.validation.ValidationError;

import javax.annotation.Priority;
import javax.inject.Singleton;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import javax.ws.rs.Priorities;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
@Singleton
public class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {

    @Override
    public Response toResponse(ValidationException e) {
        if (e instanceof ConstraintViolationException) {
            final StringBuilder strBuilder = new StringBuilder();
            for (ConstraintViolation<?> cv : ((ConstraintViolationException) e).getConstraintViolations()) {
                strBuilder.append(cv.getMessageTemplate() + " " + ((ConstraintViolationException) e).getMessage());
            }
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode()).type(MediaType.APPLICATION_JSON)
                    .entity(new ValidationError(strBuilder.toString(), "TEMPLATE","path", e.getMessage())).build();
        }
        return Response.serverError().entity(e.getMessage()).build();
    }
}