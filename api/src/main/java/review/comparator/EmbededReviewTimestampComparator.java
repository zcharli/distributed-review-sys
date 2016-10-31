package review.comparator;

import review.BaseReview;
import review.response.EmbededContainer;

import java.util.Comparator;

/**
 * Created by cli on 10/31/2016.
 */
public class EmbededReviewTimestampComparator implements Comparator<EmbededContainer>  {
    public int compare(EmbededContainer d, EmbededContainer d1) {
        return (int)(d.review.m_publishTime - d1.review.m_publishTime);
    }
}

