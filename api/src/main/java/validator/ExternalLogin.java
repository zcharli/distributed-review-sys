package validator;

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
 * Created by cli on 10/5/2016.
 */
@Target({ ElementType.PARAMETER, ElementType.TYPE_PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ExternalLogin.LoginValidator.class)
public @interface ExternalLogin {
    String message() default "Error in post parameters.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    public static class LoginValidator implements ConstraintValidator<ExternalLogin, LoginRequest> {

        @Override
        public void initialize(final ExternalLogin studentID) { }

        @Override
        public boolean isValid(final LoginRequest review, ConstraintValidatorContext ctx) {
            return review.validate();
        }
    }
}