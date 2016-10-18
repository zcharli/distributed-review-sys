package validator;

import review.request.CreateAccountRequest;
import review.request.LoginRequest;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by cli on 10/17/2016.
 */
@Target({ ElementType.PARAMETER, ElementType.TYPE_PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ExternalCreateAccount.CreateAccountValidator.class)
public @interface ExternalCreateAccount {
    String message() default "Error in post parameters.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    public static class CreateAccountValidator implements ConstraintValidator<ExternalCreateAccount, CreateAccountRequest> {

        @Override
        public void initialize(final ExternalCreateAccount studentID) { }

        @Override
        public boolean isValid(final CreateAccountRequest request, ConstraintValidatorContext ctx) {
            return request.validate();
        }
    }
}