package review.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import review.BaseReview;
import servlet.rest.MetricServlet;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by cli on 10/31/2016.
 */
public class EmbededContainer {
    public List<Integer> stars;
    public String date;
    public BaseReview review;
    public String helpful;

    @JsonIgnore
    private DateTimeFormatter fmt = DateTimeFormat.forPattern("EEEE, MMMM d, YYYY");

    public EmbededContainer() {
        this(null);
    }

    public EmbededContainer(BaseReview review) {
        if (review == null) {
            throw new NullPointerException("Review cannot be null in Embeded container");
        }
        this.review = review;
        this.stars = new ArrayList<>(review.m_stars);
        this.date = date;
        for (int i = 0; i < review.m_stars; i++) {
            this.stars.add(i);
        }
        this.date = MetricServlet.getDate(review.m_publishTime).toString(fmt);
        int totalVoters = review.m_downvotes + review.m_upvotes;
        if (totalVoters == 0) {
            this.helpful = "No one has voted on this review yet.";
        } else {
            this.helpful = review.m_upvotes + " out of " + totalVoters + " found this review helpful";
        }
    }
}
