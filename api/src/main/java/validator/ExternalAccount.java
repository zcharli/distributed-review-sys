package validator;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by cli on 10/27/2016.
 */
@Target({ ElementType.PARAMETER, ElementType.TYPE_PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ExternalAccount.AccountValidator.class)
public @interface ExternalAccount {
    String message() default "Error in post parameters.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    public static class AccountValidator implements ConstraintValidator<ExternalAccount, Validatable> {

        @Override
        public void initialize(final ExternalAccount studentID) { }

        @Override
        public boolean isValid(final Validatable account, ConstraintValidatorContext ctx) {
            return account.validate();
        }
    }
}