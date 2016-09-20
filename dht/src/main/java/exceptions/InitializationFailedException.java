package exceptions;

/**
 * Created by czl on 20/09/16.
 */
public class InitializationFailedException extends Exception {
    public InitializationFailedException() { super(); }
    public InitializationFailedException(String message) { super(message); }
    public InitializationFailedException(String message, Throwable cause) { super(message, cause); }
    public InitializationFailedException(Throwable cause) { super(cause); }
}
