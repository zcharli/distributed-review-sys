package review;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by cli on 9/30/2016.
 */
public class CommodityReview extends BaseReview {
    @JsonProperty("barcode")
    public String m_upcCode = "";
}
