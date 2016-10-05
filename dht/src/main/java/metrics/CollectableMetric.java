package metrics;

/**
 * Created by cli on 10/4/2016.
 */
public enum CollectableMetric {
    TOTAL_PUT_USAGE {
        public void track() {

        }
    },
    TOTAL_GET_USAGE {
        public void track() {

        }
    },
    ACCEPTED_DATA {
        public void track() {

        }
    },
    EXTERNAL_GETs {
        public void track() {

        }
    };

    public abstract void track();
}
