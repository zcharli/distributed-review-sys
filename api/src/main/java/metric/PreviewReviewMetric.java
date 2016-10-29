package metric;

import net.tomp2p.peers.Number160;

/**
 * Created by cli on 10/28/2016.
 */
public class PreviewReviewMetric {
    public String identifier;
    public String title;
    public String id;
    public long value;
    public String type = "preview";

    public PreviewReviewMetric() {
        this("","",0);
    }

    public PreviewReviewMetric(String identifier, String title, long views) {
        this.identifier = identifier;
        this.title = title;
        this.value = views;
        this.id = Number160.createHash(identifier + title).toString();
    }
}
