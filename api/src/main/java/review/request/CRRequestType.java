package review.request;

/**
 * Created by cli on 10/7/2016.
 */
public enum CRRequestType {
    COMMODITY {
        public String getStringValue() {
            return this.toString();
        }
    };

    abstract String getStringValue();
}
