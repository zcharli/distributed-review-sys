package key;

/**
 * Created by cli on 9/20/2016.
 */
public interface DRSKey {
    // Typical hash of an list's ID
    public abstract String getLocationKey();

    // Typical hash of an element of the list
    public abstract String getContentKey();
}
