package review.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import review.BaseReview;
import validator.Validatable;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by czl on 04/10/16.
 */
@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CommodityCRRequest.class, name = "commodity")
})
public abstract class BaseCRRequest implements Validatable {

    @NotNull(message = "Review identifier is missing or null")
    public String identifier;
    @NotNull(message = "Review body is missing or null")
    public String content;

    public int stars = -1;

    public BaseCRRequest() {}

    public abstract BaseReview buildReview();

    public boolean validate() {
        if (identifier == null) {
            return false;
        }

        if (content == null) {
            return false;
        }

        return true;
    }
}
