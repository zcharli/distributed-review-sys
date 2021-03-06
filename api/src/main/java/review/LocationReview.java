package review;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import wrapper.JsonApiFormatTuple;

import java.util.Map;

/**
 * Created by cli on 9/30/2016.
 */
public class LocationReview extends BaseReview {

    @JsonProperty("longitude")
    public long m_latitutde;

    @JsonProperty("latitude")
    public long m_longitude;

    @JsonIgnore
    public static final String m_locationType = "location";

    public LocationReview() {
        super();
    }

    public LocationReview(String review) {
        this(review, "", -1, -1, -1);
    }

    public LocationReview(String review, int stars) {
        this(review, "", stars, -1, -1);
    }

    public LocationReview(String review, String title, int stars, long lat, long lon) {
        super(review, title, stars, 0);
        m_latitutde = lat;
        m_longitude = lon;
    }

    public Map<String, Object> mapObjectForEmber(JsonApiFormatTuple.JsonApiShortRelationshipRep relationship) {
        return null;
    }

    @Override
    @JsonIgnore
    public String getIdentifier() {
        int result = (int) (m_longitude ^ (m_longitude >>> 32));
        result = 31 * result + (int) (m_latitutde ^ (m_latitutde >>> 32));
        return Integer.toString(result);
    }

    @JsonIgnore
    public String getType() {
        return m_locationType;
    }
}
