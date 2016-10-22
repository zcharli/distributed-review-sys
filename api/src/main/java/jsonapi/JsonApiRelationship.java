package jsonapi;

/**
 * Created by cli on 10/21/2016.
 */
public class JsonApiRelationship {
    public String type;
    public Object data;

    public JsonApiRelationship(String type, Object data) {
        this.type = type;
        this.data = data;
    }
}
