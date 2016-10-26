package user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nullable;

/**
 * Created by cli on 10/26/2016.
 */
public class BaseAccount {

    @JsonProperty("email")
    public String m_email;

    @JsonProperty("fname")
    @Nullable
    public String m_firstName;

    @JsonProperty("lname")
    public String m_lastName;

    @JsonIgnore
    public String m_password;

    public BaseAccount() {}

    public BaseAccount(String email, String password) {
        m_email = email;
        m_password = password;
    }
}
