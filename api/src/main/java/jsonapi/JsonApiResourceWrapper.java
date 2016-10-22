package jsonapi;

import com.fasterxml.jackson.annotation.JsonIgnore;
import review.BaseReview;

import java.util.LinkedList;
import java.util.List;

/**
 * NOT USED
 */
public class JsonApiResourceWrapper {
    public List<BaseReview> reviews;
    public String id;
    public String name;
    public String type;


    public JsonApiResourceWrapper() {
        this("", "", "");
    }

    public JsonApiResourceWrapper(String identifier, String type) {
        this(identifier, "", type);
    }

    public JsonApiResourceWrapper(String identifier, String name, String type) {
        this.id = identifier;
        this.name = name;
        this.type = type;
        reviews = new LinkedList<>();
    }

    public JsonApiResourceWrapper(String identifier, String name, String type, List<BaseReview> list) {//, SelfReferingURI url) {
        this.id = identifier;
        this.name = name;
        this.type = type;
        reviews = list;
//        requestUri = url;
    }

    @JsonIgnore
    public synchronized void add(BaseReview review) {
        reviews.add(review);
    }

    @JsonIgnore
    public synchronized void setIdentifier(String id) {
        this.id = id;
    }


    @JsonIgnore
    public static JsonApiResponseBuilder builder() {
        return new JsonApiResponseBuilder();
    }

    public static class JsonApiResponseBuilder {
        public List<BaseReview> reviews;
        public String id;
        public String name;
        public String type;
//        public SelfReferingURI requestUri;

        public JsonApiResponseBuilder() {
        }

        public JsonApiResponseBuilder reviews(List<BaseReview> reviewList) {
            this.reviews = reviewList;
            return this;
        }

        public JsonApiResponseBuilder id(String id) {
            this.id = id;
            return this;
        }

        public JsonApiResponseBuilder name(String type) {
            this.type = type;
            return this;
        }

        public JsonApiResponseBuilder type(String name) {
            this.name = name;
            return this;
        }

//        public JsonApiResponseBuilder requestUri(String uri) {
//            requestUri = new SelfReferingURI(uri);
//            return this;
//        }

        public JsonApiResourceWrapper build() {
            return new JsonApiResourceWrapper(id, name, type, reviews);//, requestUri);
        }

    }

}
