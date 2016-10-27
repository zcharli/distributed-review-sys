package review.response;

/**
 * Created by cli on 10/10/2016.
 */
public class OperationCompleteResponse<T> {
    public String status;
    public T message;

    public OperationCompleteResponse(String status, T message) {
        this.status = status;
        this.message = message;
    }
}
