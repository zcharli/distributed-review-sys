package review;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by cli on 9/27/2016.
 */
public class BaseReview {

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
}
