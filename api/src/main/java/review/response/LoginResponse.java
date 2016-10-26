package review.response;

import java.io.Serializable;

/**
 * Created by cli on 10/12/2016.
 */
public class LoginResponse<T> {
    public int status;
    public T result;
    public String responseText;

    public LoginResponse(int status, T result){
        this(status, result, "");
    }

    public LoginResponse(int status, T result, String responseText) {
        this.status = status;
        this.responseText = responseText;
        this.result = result;
    }
}
