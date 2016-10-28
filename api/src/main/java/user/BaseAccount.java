package user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import config.APIConfig;
import net.tomp2p.peers.Number160;
import validator.Validatable;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;


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
    public String m_loginToken;

    @JsonProperty("tokens")
    Deque<String> m_prevTokens;

    public BaseAccount() {
        m_prevTokens = new LinkedList<>();
    }

    public BaseAccount(String email, String password) {
        m_email = email;
        m_password = password;
        m_userId = Number160.createHash(email).toString();
        m_prevTokens = new LinkedList<>();
    }

    public boolean validate() {
        if (Strings.isNullOrEmpty(m_userId) || Strings.isNullOrEmpty(m_email)) {
            return false;
        }
        return true;
    }

    public void addToken(Long token) {
        m_loginToken = token.toString();
        if (m_prevTokens.size() > APIConfig.MAX_TOKEN_SESSIONS) {
            m_prevTokens.removeLast();
        }
        m_prevTokens.addFirst(m_loginToken);
    }

    public boolean hasToken(String token) {
        return m_prevTokens.contains(token);
    }
}
