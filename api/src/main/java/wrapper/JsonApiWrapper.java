package wrapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by cli on 10/20/2016.
 *
 * Top level Json Api response
 */
public class JsonApiWrapper {
    private final static Logger LOGGER = LoggerFactory.getLogger(JsonApiWrapper.class);

    @JsonProperty("id")
    public String locationId;

    @JsonProperty("type")
    public String modelType;

    @JsonProperty("links")
    public SelfReferingURI requestUri;

    @JsonProperty("attributes")
    public Map<String, String> attributeMapping;

    @JsonProperty("relationships")
    public Map<String, Map<String, Collection<JsonApiFormatTuple.JsonApiShortRelationshipRep>>> relationshipMapping;
//
//    @JsonProperty("included")
//    public LinkedList<Map<String, Object>> includedPayload;

    public JsonApiWrapper() {
        relationshipMapping = new HashMap<>();
        attributeMapping = new HashMap<>();
//        includedPayload = new LinkedList<>();
    }
    public JsonApiWrapper(Collection<JsonApiResourceWrapper> list, SelfReferingURI url, HashMap<String, String> attrs, HashMap<String, Map<String, Collection<JsonApiFormatTuple.JsonApiShortRelationshipRep>>> relationships) {
        this.requestUri = url;
        this.attributeMapping = attrs == null ? new HashMap<>() :  attrs;
        this.relationshipMapping = relationships == null ? new HashMap<>() : relationships;
    }

    @JsonIgnore
    public JsonApiWrapper setModelType(String type) {
        this.modelType = type;
        return this;
    }

    @JsonIgnore
    public JsonApiWrapper setModelId(String id) {
        this.locationId = id;
        return this;
    }

    @JsonIgnore
    public JsonApiWrapper setRequestUri(String uri) {
        requestUri = new SelfReferingURI(uri);
        return this;
    }

    @JsonIgnore
    public JsonApiWrapper setRequestUri(SelfReferingURI uri) {
        requestUri = uri;
        return this;
    }

    @JsonIgnore
    public boolean checkHasName() {
        if (attributeMapping == null) {
            return false;
        }
        return attributeMapping.containsKey("name");
    }

    @JsonIgnore
    public JsonApiWrapper setRelationshipData(String model, Map<String, Collection<JsonApiFormatTuple.JsonApiShortRelationshipRep>> relationships) {
        if (relationshipMapping != null) {
            relationshipMapping.put(model, relationships);
        }
        return this;
    }

    @JsonIgnore
    public JsonApiWrapper addToRelationshipData(String model, JsonApiFormatTuple.JsonApiShortRelationshipRep relationship) {
        if (relationshipMapping == null) {
            relationshipMapping = new HashMap<>();
        }
        if (!relationshipMapping.containsKey(model)) {
            relationshipMapping.put(model, new HashMap<String, Collection<JsonApiFormatTuple.JsonApiShortRelationshipRep>>());
        }

        if (!relationshipMapping.get(model).containsKey("data")) {
            relationshipMapping.get(model).put("data", new LinkedList<>());
        }
        relationshipMapping.get(model).get("data").add(relationship);
        return this;
    }

    @JsonIgnore
    public JsonApiWrapper putAttribute(String key, String value) {
        attributeMapping.put(key, value);
        return this;
    }

//    @JsonIgnore
//    public JsonApiWrapper addPayload(Map<String, Object> objectMapping) {
//        includedPayload.add(objectMapping);
//        return this;
//    }

    public static class SelfReferingURI {
        public String self;

        public SelfReferingURI(String uri) {
            self = uri;
        }
    }
}
