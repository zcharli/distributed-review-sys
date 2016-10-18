package review.response;

import java.io.Serializable;

/**
 * Created by cli on 10/12/2016.
 */
public class LoginResponse implements Serializable {
    public int status;
    public String responseText;
    public String clientId;

    public LoginResponse(int status, String responseText){
        this(status, responseText, null);
    }

    public LoginResponse(int status, String responseText, String clientId) {
        this.status = status;
        this.responseText = responseText;
        this.clientId = clientId;
    }
}
