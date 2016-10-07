package validator;

import request.CreateReviewRequest;

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
@Constraint(validatedBy = ExternalReview.ReviewValidator.class)
public @interface ExternalReview {
    String message() default "Error in post parameters.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    public static class ReviewValidator implements ConstraintValidator<ExternalReview, CreateReviewRequest> {


        @Override
        public void initialize(final ExternalReview studentID) {
            System.out.println("init review validation");
        }

        @Override
        public boolean isValid(final CreateReviewRequest review, ConstraintValidatorContext ctx) {
            if (review.identifier == null) {
                return false;
            }
            if (review.review == null) {
                return false;
            }
            return true;
        }
    }
}