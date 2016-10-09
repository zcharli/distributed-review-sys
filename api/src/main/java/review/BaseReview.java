package review;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import review.request.BaseCRRequest;

import java.io.Serializable;

/**
 * Created by cli on 9/27/2016.
 */
public abstract class BaseReview implements Serializable {

    @JsonProperty("review_content")
    public String m_content;

    @JsonProperty("created_at")
    public long m_createTime;

    @JsonProperty("stars")
    public float m_stars;

    public BaseReview() {
        m_createTime = System.nanoTime();
        m_stars = -1;
        m_content = "";
    }

    public BaseReview(String content, float stars) {
        m_createTime = System.nanoTime();
        m_content = content;
        m_stars = stars;
    }

    public BaseReview(BaseCRRequest request) {
        m_createTime = System.nanoTime();
        m_stars = request.stars;
        m_content = request.content;
    }

    @JsonIgnore
    public abstract String getIdentifier();

    @JsonIgnore
    public String getContent() {
        return m_content;
    }
}
