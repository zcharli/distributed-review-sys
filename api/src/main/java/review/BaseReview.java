package review;

import com.fasterxml.jackson.annotation.*;
import com.google.common.base.Strings;
import wrapper.JsonApiFormatTuple;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
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
    @Nullable
    @JsonProperty("description")
    public String m_productName; // Special uneeded

    @JsonProperty("type")
    public static final String m_generalReviewType = "review"; // Maps on to Ember's model

    /*
    Below are the normal attributes
     */
    @JsonProperty("id")
    public String m_dhtAbsoluteKey;

    @JsonProperty("review_content")
    @NotNull(message = "Review body is missing or null")
    public String m_content;

    @JsonProperty("title")
    @NotNull(message = "A review title is missing or null")
    public String m_title;

    @JsonProperty("created_at")
    public long m_createTime;

    @JsonProperty("stars")
    public int m_stars;

    @JsonProperty("upvotes")
    public int m_upvotes;

    @JsonProperty("downvotes")
    public int m_downvotes;

    /*
    Content Keys below
     */
    @JsonIgnore
    public Number160 m_contentId;
    @JsonProperty("contentId")
    public String getContentId() {
        return m_contentId == null ? "" : m_contentId.toString();
    }
    @JsonSetter("contentId")
    public void setContentId(String content) {
        m_contentId = new Number160(content);
    }

    @JsonIgnore
    public Number160 m_locationId;
    @JsonProperty("locationId")
    public String getLocationId() {
        return m_locationId != null ? m_locationId.toString(true) : "";
    }
    @JsonSetter("locationId")
    public void setLocationId(String location) {
        m_locationId = new Number160(location);
    }

    @JsonIgnore
    public Number160 m_domainId;
    @JsonProperty("domainId")
    public String getDomainId() {
        return m_domainId != null ? m_domainId.toString(true) : "";
    }
    @JsonSetter("domainId")
    public void setDomainId(String domain) {
        m_domainId = new Number160(domain);
    }

    @JsonIgnore
    public Number640 m_dhtKey;

    @JsonProperty("publish_time")
    public long m_publishTime;

    /*
    Core class functionality
     */
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
        m_downvotes = 0;
        m_title = title;
    }

    @JsonIgnore
    public void fillInIds(Number160 loc, Number160 con, Number160 dom, Number640 absoluteKey) {
        m_locationId = loc;
        m_contentId = con;
        m_domainId = dom;
        m_dhtKey = absoluteKey;
        m_dhtAbsoluteKey = Number160.createHash(absoluteKey.toString()).toString();
    }

    @JsonIgnore
    public abstract String getIdentifier();

    @JsonIgnore
    public abstract String getType();

    @JsonIgnore
    public String getAbsoluteId() {
        return Strings.isNullOrEmpty(m_dhtAbsoluteKey) ? "" : m_dhtAbsoluteKey;
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

    @JsonIgnore
    public abstract Map<String, Object> mapObjectForEmber(JsonApiFormatTuple.JsonApiShortRelationshipRep relationship);

    public boolean validate() {
        if (Strings.isNullOrEmpty(m_content) || Strings.isNullOrEmpty(m_title)) {
            return false;
        }
        return true;
    }
}
