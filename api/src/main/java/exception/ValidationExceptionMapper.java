package exception;

/**
 * Created by cli on 10/5/2016.
 */
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
@Singleton
public class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {

    @Context
    HttpServletRequest request;

    @Override
    public Response toResponse(ValidationException e) {
        if (e instanceof ConstraintViolationException) {
            final StringBuilder strBuilder = new StringBuilder();
            for (ConstraintViolation<?> cv : ((ConstraintViolationException) e).getConstraintViolations()) {
                strBuilder.append(cv.getMessageTemplate());
            }
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode()).type(MediaType.APPLICATION_JSON)
                    .entity(new ValidationError(strBuilder.toString(), request.getRequestURI(), "Likely missing required parameters")).build();
        }
        return Response.serverError().entity(e.getMessage()).build();
    }
}