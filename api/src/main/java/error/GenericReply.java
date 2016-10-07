package error;

/**
 * Used to capture generic error to wrap in an object for json serialization
 *
 * Created by czl on 04/10/16.
 */
public class GenericReply<T> {
    public String errorCode;
    public T errorDetails;

    public GenericReply(String errorCode, T errorDetails) {
        this.errorCode = errorCode;
        this.errorDetails = errorDetails;
    }
}
