package review;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by cli on 10/19/2016.
 */
public class ProductRESTWrapper {
    public List<BaseReview> reviews;
    public String identifier;
    public String name;

    public ProductRESTWrapper() {
        this("", "");
    }

    public ProductRESTWrapper(String identifier) {
        this(identifier, "");
    }

    public ProductRESTWrapper(String identifier, String name) {
        this.identifier = identifier;
        this.name = name;
        reviews = new LinkedList<>();
    }

    @JsonIgnore
    public synchronized void add(BaseReview review) {
        reviews.add(review);
    }

    @JsonIgnore
    public synchronized void setIdentifier(String id) {
        identifier = id;
    }
}
