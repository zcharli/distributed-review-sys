package review.response;

/**
 * Created by cli on 10/10/2016.
 */
public class ReviewOperationComplete<T> {
    public String status;
    public T message;

    public ReviewOperationComplete(String status, T message) {
        this.status = status;
        this.message = message;
    }
}
