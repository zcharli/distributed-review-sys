package review;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Created by cli on 10/22/2016.
 */
public class ProductReviewWrapper {

    public String name;

    public String id;

    public String identifier;

    public Collection<BaseReview> reviews;

    public ProductReviewWrapper() {
        reviews = new LinkedList<>();
    }

    public ProductReviewWrapper setId(String id) {
        this.id = id;
        return this;
    }

    public ProductReviewWrapper setIdentifier(String id) {
        this.identifier = id;
        return this;
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
