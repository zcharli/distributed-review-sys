package review;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by cli on 9/30/2016.
 */
public class CommodityReview extends BaseReview {

    @JsonProperty("barcode")
    public String m_upcCode;

    public CommodityReview() {
        this("","",-1);
    }

    public CommodityReview(String review, String barcode) {
        this(review, barcode, -1);
    }

    public CommodityReview(String review, String barcode, int stars) {
        super(review, stars);
        m_upcCode = review;
    }
}
