package core;

import exception.ApplicationExceptionMapper;
import exception.JsonMappingExceptionMapper;
import exception.JsonProcessingExceptionMapper;
import exception.ValidationExceptionMapper;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.validation.ValidationFeature;
import servlet.rest.AccountServlet;
import servlet.rest.ReviewServlet;
import validator.ValidationConfigContext;
/**
 * Created by cli on 10/5/2016.
 */
public class APIResourceConfig extends ResourceConfig {

    public APIResourceConfig() {
        packages("my.package");
        packages(AccountServlet.class.getPackage().getName());
        packages(ReviewServlet.class.getPackage().getName());
        register(ValidationConfigContext.class);

        register(ValidationExceptionMapper.class);
        register(JsonProcessingExceptionMapper.class);
        register(JsonMappingExceptionMapper.class);
        register(ApplicationExceptionMapper.class);
        register(CORSResponseFilter.class);

        register(JacksonFeature.class);
        register(ValidationFeature.class);
        property(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true);
        property(ServerProperties.BV_DISABLE_VALIDATE_ON_EXECUTABLE_OVERRIDE_CHECK, true);
//        register(MoxyJsonFeature.class);
//        register(new MoxyJsonConfig().setFormattedOutput(true)
//                // Turn off BV otherwise the entities on server would be validated by MOXy as well.
//                .property(MarshallerProperties.BEAN_VALIDATION_MODE, BeanValidationMode.NONE)
//                .resolver());
    }

}
