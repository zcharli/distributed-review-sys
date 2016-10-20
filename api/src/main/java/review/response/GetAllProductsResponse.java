package review.response;

import review.ProductRESTWrapper;

import java.util.List;

/**
 * Created by cli on 10/20/2016.
 */
public class GetAllProductsResponse {
    public List<ProductRESTWrapper> product;

    public GetAllProductsResponse(List<ProductRESTWrapper> list) {
        product = list;
    }
}
