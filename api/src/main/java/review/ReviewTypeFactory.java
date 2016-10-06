package review;

import request.CreateReviewRequest;

/**
 * A mapper/factory for concrete review classes
 *
 * Created by cli on 10/5/2016.
 */
public enum ReviewTypeFactory {

    COMMODITY_REVIEW {
        public BaseReview manufacture(CreateReviewRequest request){
            return new CommodityReview(request.review, request.identifier);
        }
    },
    RESTAURANT_REVIEW {
        public BaseReview manufacture(CreateReviewRequest request){

            return null;
        }
    };

    public abstract BaseReview manufacture(CreateReviewRequest request);
}
