package review;

import com.fasterxml.jackson.annotation.*;
import com.google.common.base.Strings;
import jsonapi.JsonApiFormatTuple;
import net.tomp2p.peers.Number160;
import validator.Validatable;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Map;

/**
 * Created by cli on 9/27/2016.
 */
@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CommodityReview.class, name = "commodity")
})
public abstract class BaseReview implements Serializable, ReviewIdentity, Validatable {

    /*
    Some special attributes
     */
    @JsonIgnore
    @Nullable
    public String m_productName; // Special

    @JsonProperty("type")
    public static final String m_generalReviewType = "review"; // Maps on to Ember's model

    /*
    Below are the normal attributes
     */
    @JsonProperty("review_content")
    @NotNull(message = "Review body is missing or null")
    public String m_content;

    @JsonProperty("created_at")
    public long m_createTime;

    @JsonProperty("stars")
    public int m_stars;

    @JsonProperty("title")
    @NotNull(message = "A review title is missing or null")
    public String m_title;

    @JsonProperty("upvotes")
    public int m_upvotes;

    @JsonProperty("id")
    public Number160 m_contentId;

    @JsonIgnore
    public Number160 m_locationId;

    @JsonIgnore
    public Number160 m_domainId;

    public BaseReview() {
        this("", "", 0, 0);
    }

    public BaseReview(String content, int stars) {
        this(content, "", stars, 0);
    }

    public BaseReview(String content, String title, int stars) {
        this(content, title, stars, 0);
    }

    public BaseReview(String content, String title, int stars, int votes) {
        m_createTime = System.currentTimeMillis();
        m_content = content;
        m_stars = stars;
        m_upvotes = votes;
        m_title = title;
    }

    @JsonIgnore
    public abstract String getIdentifier();

    @JsonIgnore
    public abstract Map<String, Object> mapObjectForEmber(JsonApiFormatTuple.JsonApiShortRelationshipRep relationship);

    @JsonIgnore
    public String getContentId() {
        return m_contentId != null ? m_contentId.toString(true) : "";
    }

    @JsonIgnore
    public String getDomainId() {
        return m_domainId != null ? m_domainId.toString(true) : "";
    }

    @JsonIgnore
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
    public String getContent() {
        return m_content;
    }

    public BaseReview identity() {
        return this;
    }

    @JsonIgnore
    public String getModelId() {
        return m_content == null ? "" : m_content.toString();
    }

    public boolean validate() {

        if (Strings.isNullOrEmpty(m_content) || Strings.isNullOrEmpty(m_title)) {
            return false;
        }

        return true;
    }
}
