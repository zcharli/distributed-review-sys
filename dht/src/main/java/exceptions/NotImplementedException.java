package exceptions;

/**
 * Created by cli on 9/20/2016.
 */
public class NotImplementedException extends Exception {
    public NotImplementedException() { super(); }
    public NotImplementedException(String message) { super(message); }
    public NotImplementedException(String message, Throwable cause) { super(message, cause); }
    public NotImplementedException(Throwable cause) { super(cause); }
}
