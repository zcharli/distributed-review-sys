package jsonapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by cli on 10/21/2016.
 */
public class JsonApiFormatTuple<SAMPLE, FULL> {

    private final static Logger LOGGER = LoggerFactory.getLogger(JsonApiFormatTuple.class);

    public SAMPLE shortRepresentation;
    public FULL fullRepresentation;

    public JsonApiFormatTuple(SAMPLE sample, FULL full) {
        if (sample == null || full == null) {
            LOGGER.error("Created a JsonApiFormatTuple with a null parameter");
        }
        shortRepresentation = sample;
        fullRepresentation = full;
    }

    public static class JsonApiShortRelationshipRep {
        public String type;
        public String id;

        public JsonApiShortRelationshipRep(String type, String id) {
            this.type = type;
            this.id = id;
        }
    }
}
