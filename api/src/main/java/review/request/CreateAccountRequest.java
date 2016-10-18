package review.request;

import com.google.common.base.Strings;
import validator.Validatable;

import javax.validation.constraints.NotNull;

/**
 * Created by cli on 10/17/2016.
 */
public class CreateAccountRequest implements Validatable {
    @NotNull(message = "Identification/email cannot be null.")
    public String identification;

    @NotNull(message = "A password must be supplied")
    public String password;

    public boolean validate() {
        if (Strings.isNullOrEmpty(identification) || Strings.isNullOrEmpty(password)) {
            return false;
        }
        return true;
    }
}
