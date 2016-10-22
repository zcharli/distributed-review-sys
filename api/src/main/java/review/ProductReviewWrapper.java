package review;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Created by cli on 10/22/2016.
 */
public class ProductReviewWrapper {

    public String name;

    public Collection<BaseReview> reviews;

    public ProductReviewWrapper() {
        reviews = new LinkedList<>();
    }

    public ProductReviewWrapper setName(String name) {
        this.name = name;
        return this;
    }

    public ProductReviewWrapper add(BaseReview review) {
        reviews.add(review);
        return this;
    }
}
