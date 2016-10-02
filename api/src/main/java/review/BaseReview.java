package review;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by cli on 9/27/2016.
 */
public class BaseReview {

    @JsonProperty("review_content")
    public String m_content = "Hello world";

    public BaseReview() {}
}
