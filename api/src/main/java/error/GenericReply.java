package error;

import java.io.Serializable;

/**
 * Used to capture generic error to wrap in an object for json serialization
 *
 * Created by czl on 04/10/16.
 */
public class GenericReply<T> implements Serializable {
    public String status;
    public T responseText;

    public GenericReply(String errorCode, T responseText) {
        this.status = errorCode;
        this.responseText = responseText;
    }
}
