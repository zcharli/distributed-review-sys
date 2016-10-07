package exception;

/**
 * Created by cli on 10/6/2016.
 */

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public final class ValidationError {

    private String message;

    private String path;

    private String invalidValue;

    public ValidationError() {
    }

    public ValidationError(final String message, final String path, final String invalidValue) {
        this.message = message;
        this.path = path;
        this.invalidValue = invalidValue;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public String getInvalidValue() {
        return invalidValue;
    }

    public void setInvalidValue(final String invalidValue) {
        this.invalidValue = invalidValue;
    }
}
