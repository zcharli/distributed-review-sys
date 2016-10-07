package review;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by cli on 9/30/2016.
 */
public class LocationReview extends BaseReview {

    @JsonProperty("longitude")
    public long m_latitutde;

    @JsonProperty("latitude")
    public long m_longitude;

    public LocationReview() {
        super();
    }

    public LocationReview(String review) {
        this(review, -1, -1, -1);
    }

    public LocationReview(String review, float stars) {
        this(review, stars, -1, -1);
    }

    public LocationReview(String review, float stars, long lat, long lon) {
        super(review, stars);
        m_latitutde = lat;
        m_longitude = lon;
    }

    @Override
    @JsonIgnore
    public String getIdentifier() {
        int result = (int) (m_longitude ^ (m_longitude >>> 32));
        result = 31 * result + (int) (m_latitutde ^ (m_latitutde >>> 32));
        return Integer.toString(result);
    }
}