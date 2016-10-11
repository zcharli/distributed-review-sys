package review.response;

import review.BaseReview;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by cli on 10/9/2016.
 */
public class ReviewGetResponse {
    public int status;
    public List<BaseReview> results;

    public ReviewGetResponse(int status) {
        this(status, new LinkedList<>());
    }

    public ReviewGetResponse(int statuts, List<BaseReview> list) {
        this.status = status;
        this.results = list;
    }
}
