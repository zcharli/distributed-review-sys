package wrapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import review.BaseReview;
import review.ProductReviewWrapper;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Created by cli on 10/22/2016.
 */
public class ProductRestWrapper {

    @JsonProperty("type")
    public static final String type = "products";

//    @JsonProperty("identifier")
//    public String productIdentity;

    @Nullable
    public Collection<ProductReviewWrapper> products;

    public ProductRestWrapper() {}


    public ProductRestWrapper setProducts(Collection<ProductReviewWrapper> products) {
        if (this.products == null) {
            this.products = products;
        } else {
            this.products.addAll(products);
        }
        return this;
    }

//    public ProductRestWrapper setIdentifier(String id) {
//        this.productIdentity = id;
//        return this;
//    }

    public ProductRestWrapper add(ProductReviewWrapper product) {
        if (this.products == null) {
            this.products = new LinkedList<>();
        }
        products.add(product);
        return this;
    }
}
