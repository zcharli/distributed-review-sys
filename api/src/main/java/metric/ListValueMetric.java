package metric;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by cli on 10/28/2016.
 */
public class ListValueMetric extends BaseMetric {

    public List<PreviewReviewMetric> previews;

    public ListValueMetric() {
        previews = new LinkedList<>();
    }

    public ListValueMetric setReview(List<PreviewReviewMetric> in) {
        this.previews = in;
        return this;
    }

    public ListValueMetric addReview(PreviewReviewMetric in) {
        this.previews.add(in);
        return this;
    }

    public ListValueMetric setId(String id) {
        this.id = id;
        return this;
    }

    public ListValueMetric setName(String name) {
        this.name = name;
        return this;
    }

    public ListValueMetric setMetricType(MetricType metricType) {
        super.setMetricType(metricType);
        return this;
    }

    public ListValueMetric setComponent(String in) {
        component = in;
        return this;
    }

    public ListValueMetric setIcon(String in) {
        icon = in;
        return this;
    }

    public ListValueMetric setDescription(String in) {
        description = in;
        return this;
    }

    public ListValueMetric setPagePosition(String in) {
        position = in;
        return this;
    }
}
