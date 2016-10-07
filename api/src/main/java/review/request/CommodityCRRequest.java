package review.request;

import review.BaseReview;
import review.CommodityReview;

/**
 * Created by cli on 10/7/2016.
 */
public class CommodityCRRequest extends BaseCRRequest {

    public String barcode;

    public CommodityCRRequest() {
        super();
    }

    @Override
    public BaseReview buildReview() {
        return new CommodityReview(this);
    }

    @Override
    public boolean validate() {
        if (barcode == null) {
            return false;
        }

        return super.validate();
    }
}
