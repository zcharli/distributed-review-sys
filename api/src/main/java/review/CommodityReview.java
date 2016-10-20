package review;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

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
