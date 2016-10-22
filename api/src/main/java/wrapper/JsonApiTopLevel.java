package wrapper;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
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
    public JsonApiTopLevel addPayload(List<Map<String, Object>> objectMapping) {
        included.addAll(objectMapping);
        return this;
    }
}
