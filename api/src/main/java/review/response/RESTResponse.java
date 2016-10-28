package review.response;


/**
 * Created by cli on 10/28/2016.
 */
public interface RESTResponse {

    RESTResponse setId(String id);

    RESTResponse setIdentifier(String id);

    RESTResponse setName(String name);

    RESTResponse setType(String type);
}
