package validator;

import org.glassfish.jersey.server.validation.ValidationConfig;
import org.glassfish.jersey.server.validation.internal.InjectingConstraintValidatorFactory;

import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.ContextResolver;

/**
 * Created by cli on 10/5/2016.
 */
public class ValidationConfigContext implements ContextResolver<ValidationConfig> {
    @Context
    private ResourceContext resourceContext;
    @Override
    public ValidationConfig getContext(final Class<?> type) {
        return new ValidationConfig().constraintValidatorFactory(resourceContext.getResource(InjectingConstraintValidatorFactory.class));
    }
}