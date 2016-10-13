package review.response;

/**
 * Created by cli on 10/12/2016.
 */
public class LoginResponse {
    int status;
    String responseText;
    String clientId;

    public LoginResponse(int status, String responseText){
        this(status, responseText, null);
    }

    public LoginResponse(int status, String responseText, String clientId) {
        this.status = status;
        this.responseText = responseText;
        this.clientId = clientId;
    }
}
