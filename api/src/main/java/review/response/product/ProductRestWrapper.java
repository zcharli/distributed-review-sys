package review.response.product;

import com.fasterxml.jackson.annotation.JsonProperty;
import review.ProductReviewWrapper;
import review.response.BaseRestWrapper;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Created by cli on 10/22/2016.
 */
public class ProductRestWrapper extends BaseRestWrapper {

    @JsonProperty("type")
    public final String type = "products";

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
    public ProductRestWrapper add(ProductReviewWrapper product) {
        if (this.products == null) {
            this.products = new LinkedList<>();
        }
        products.add(product);
        return this;
    }
}
