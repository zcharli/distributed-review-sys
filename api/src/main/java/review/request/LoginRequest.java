package review.request;

import javax.validation.constraints.NotNull;

/**
 * Created by cli on 10/12/2016.
 */
public class LoginRequest {
    @NotNull(message = "Username must not be null")
    public String username;

    @NotNull(message = "Password must not be null")
    public String password;

    public boolean validate() {
        return username != null && username.length() > 5 &&
                password != null && password.length() > 5;
    }
}
