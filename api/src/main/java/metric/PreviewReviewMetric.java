package metric;

import config.APIConfig;
import net.tomp2p.peers.Number160;
import review.BaseReview;
import review.ProductReviewWrapper;

/**
 * Created by cli on 10/28/2016.
 */
public class PreviewReviewMetric {
    public String identifier;
    public String title;
    public String id;
    public long value;
    public String type = "preview";
    public String[] url;

    public PreviewReviewMetric() { }

    public PreviewReviewMetric(ProductReviewWrapper product, long views) {
        this.identifier = product.identifier;
        this.title = product.name.length() > APIConfig.PREVIEW_TITLE_LENGTH
                ? product.name.substring(0, APIConfig.PREVIEW_TITLE_LENGTH) + "..." : product.name;
        this.value = views;
        this.id = Number160.createHash(identifier + title).toString();
        this.url = new String[] {"product.show", product.id};
    }

    public PreviewReviewMetric(BaseReview review, long views) {
        this.identifier = review.getIdentifier();
        this.title = review.m_title.length() > APIConfig.PREVIEW_TITLE_LENGTH
                ? review.m_title.substring(0, APIConfig.PREVIEW_TITLE_LENGTH) + "..." : review.m_title;
        this.value = views;
        this.id = Number160.createHash(identifier + title).toString();
        this.url = new String[] {"product.review", review.m_dhtAbsoluteKey};
    }

    private String getUrlBase() {
        StringBuilder sb = new StringBuilder();
        sb.append(APIConfig.DEFAULT_HOST).append("/api");
        return sb.toString();
    }
}
