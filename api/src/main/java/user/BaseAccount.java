package user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import net.tomp2p.peers.Number160;
import validator.Validatable;


/**
 * Created by cli on 10/26/2016.
 */
public class BaseAccount implements Validatable {

    @JsonProperty("user_id")
    public String m_userId;

    @JsonProperty("email")
    public String m_email;

    @JsonProperty("fname")
    public String m_firstName;

    @JsonProperty("lname")
    public String m_lastName;

    @JsonProperty("password")
    public String m_password;

    @JsonProperty("profile")
    public String m_profilePicUrl;

    @JsonProperty("token")
    public Long m_loginToken;

    public BaseAccount() {}

    public BaseAccount(String email, String password) {
        m_email = email;
        m_password = password;
        m_userId = Number160.createHash(email).toString();
    }

    public boolean validate() {
        if (Strings.isNullOrEmpty(m_userId) || Strings.isNullOrEmpty(m_email)) {
            return false;
        }
        return true;
    }
}
