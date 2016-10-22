package jsonapi;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by cli on 10/21/2016.
 */
public class JsonApiTopLevel {
    public Collection<Object> data;
    public Collection<Object> included;

    public JsonApiTopLevel() {
        data = new LinkedList<>();
        included = new LinkedList<>();
    }

    public JsonApiTopLevel(Collection<Object> data, Collection<Object> included) {
        this.data = data;
        this.included = included;
    }

    @JsonIgnore
    public JsonApiTopLevel addData(JsonApiWrapper dataPortion) {
        data.add(dataPortion);
        return this;
    }

    @JsonIgnore
    public JsonApiTopLevel addPayload(Map<String, Object> objectMapping) {
        included.add(objectMapping);
        return this;
    }
}
