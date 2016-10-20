package review.response;

import review.ProductRESTWrapper;

import java.util.Collection;
import java.util.List;

/**
 * Created by cli on 10/20/2016.
 */
public class GetAllProductsResponse {
    public Collection<ProductRESTWrapper> product;

    public GetAllProductsResponse(Collection<ProductRESTWrapper> list) {
        product = list;
    }
}
