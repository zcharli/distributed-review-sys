package review;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.tomp2p.peers.Number160;
import review.request.BaseCRRequest;

import java.io.Serializable;

/**
 * Created by cli on 9/27/2016.
 */
public abstract class BaseReview implements Serializable, ReviewIdentity {

    @JsonProperty("review_content")
    public String m_content;

    @JsonProperty("created_at")
    public long m_createTime;

    @JsonProperty("stars")
    public float m_stars;

    @JsonProperty("title")
    public String m_title;

    @JsonProperty("upvotes")
    public int m_upvotes;

    @JsonIgnore
    public Number160 m_locationId;

    @JsonIgnore
    public Number160 m_contentId;

    @JsonIgnore
    public Number160 m_domainId;

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

    @JsonProperty("contentId")
    public String getContentId() {
        return m_contentId != null ? m_contentId.toString(true) : "";
    }

    @JsonProperty("domainId")
    public String getDomainId() {
        return m_domainId != null ? m_domainId.toString(true) : "";
    }

    @JsonProperty("locationId")
    public String getLocationId() {
        return m_locationId != null ? m_locationId.toString(true) : "";
    }

    @JsonIgnore
    public void fillInIds(Number160 loc, Number160 con, Number160 dom) {
        m_locationId = loc;
        m_contentId = con;
        m_domainId = dom;
    }

    @JsonIgnore
    public abstract String getIdentifier();

    @JsonIgnore
    public String getContent() {
        return m_content;
    }

    public BaseReview identity() {
        return this;
    }
}
