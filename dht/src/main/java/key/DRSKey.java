package key;

/**
 * Created by cli on 9/20/2016.
 */
public interface DRSKey {

    int MAX_KEY_LENGTH = 42;

    // Typical hash of an list's ID
    String getLocationKey();

    // Typical hash of an element of the list
    String getContentKey();
}
