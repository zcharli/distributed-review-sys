package review.comparator;

import review.BaseReview;

import java.util.Comparator;

/**
 * Created by czl on 09/10/16.
 */
public class ReviewTimestampComparator implements Comparator<BaseReview> {

    public int compare(BaseReview d, BaseReview d1) {
        return (int)(d.m_publishTime - d1.m_publishTime);
    }
}
