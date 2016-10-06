package request;

import validator.ExternalReview;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import java.lang.annotation.Annotation;

/**
 * Created by czl on 04/10/16.
 */
@XmlRootElement
public class CreateReviewRequest{

    @NotNull(message = "Review identifier is missing or null")
    public String identifier;
    @NotNull(message = "Review body is missing or null")
    public String review;

    public int stars = -1;

    public CreateReviewRequest() {
        System.out.println("Created new requevire quest");
    }

    public boolean isValid() {
        if (identifier == null) {
            return false;
        }

        if (review == null) {
            return false;
        }

        return true;
    }
}
