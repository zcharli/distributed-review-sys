package review;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jsonapi.JsonApiFormatTuple;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by cli on 9/30/2016.
 */
public class CommodityReview extends BaseReview {

    @JsonProperty("barcode")
    @NotNull(message = "Review barcode is missing or null")
    public String m_upcCode;

    public CommodityReview() {
        this("","","",-1);
    }

    public CommodityReview(String review, String title, String barcode) {
        this(review, "", barcode, -1);
    }

    public CommodityReview(String review, String title, String barcode, int stars) {
        super(review, title, stars);
        m_upcCode = review;
    }

    @Override
    public Map<String, Object> mapObjectForEmber(JsonApiFormatTuple.JsonApiShortRelationshipRep relationship) {
        // Currently this is usually done once, so we can nullify the map after function call stack
        Map<String, Object> includedPayloadMap = new HashMap<>();
        Map<String, Object> attributeMap = new HashMap<>();

        // All the things that we need to satisfy JSON API
        attributeMap.put("id", getContentId());
        attributeMap.put("domainId", m_domainId.toString());
        attributeMap.put("locationId", m_locationId.toString());
        attributeMap.put("title", m_title);
        attributeMap.put("content", m_content);
        attributeMap.put("stars", m_stars);
        attributeMap.put("created", m_createTime);
        attributeMap.put("upvotes", m_upvotes);

        includedPayloadMap.put("id", getContentId());
        includedPayloadMap.put("type", "review");
        includedPayloadMap.put("attributes",  attributeMap);
        includedPayloadMap.put("relationships", relationship);
        return includedPayloadMap;
    }

    @Override
    @JsonIgnore
    public String getIdentifier() {
        return m_upcCode;
    }

    @Override
    public boolean validate() {
        if (m_upcCode == null) {
            return false;
        }

        return super.validate();
    }
}
