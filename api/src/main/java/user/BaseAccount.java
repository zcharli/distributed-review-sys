package user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.tomp2p.peers.Number160;


/**
 * Created by cli on 10/26/2016.
 */
public class BaseAccount {

    @JsonProperty("id")
    public String m_userId;

    @JsonProperty("email")
    public String m_email;

    @JsonProperty("fname")
    public String m_firstName;

    @JsonProperty("lname")
    public String m_lastName;

    @JsonIgnore
    public String m_password;

    public BaseAccount() {}

    public BaseAccount(String email, String password) {
        m_email = email;
        m_password = password;
        m_userId = Number160.createHash(email).toString();
    }
}
