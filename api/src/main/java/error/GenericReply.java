package error;

/**
 * Used to capture generic error to wrap in an object for json serialization
 *
 * Created by czl on 04/10/16.
 */
public class GenericReply<T> {
    public String status;
    public T responseText;

    public GenericReply(String errorCode, T responseText) {
        this.status = errorCode;
        this.responseText = responseText;
    }
}
