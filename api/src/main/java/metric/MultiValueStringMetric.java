package metric;

/**
 * Created by cli on 10/29/2016.
 */
public class MultiValueStringMetric extends BaseMetric {

    public String[] labels;
    public String[] values;

    public MultiValueStringMetric() { }

    public MultiValueStringMetric setLabels(String[] in) {
        this.labels = in;
        return this;
    }

    public MultiValueStringMetric setData(String[] in) {
        this.values = in;
        return this;
    }

    public MultiValueStringMetric setId(String id) {
        this.id = id;
        return this;
    }

    public MultiValueStringMetric setName(String name) {
        this.name = name;
        return this;
    }

    public MultiValueStringMetric setMetricType(MetricType metricType) {
        super.setMetricType(metricType);
        return this;
    }

    public MultiValueStringMetric setComponent(String in) {
        component = in;
        return this;
    }

    public MultiValueStringMetric setIcon(String in) {
        icon = in;
        return this;
    }

    public MultiValueStringMetric setDescription(String in) {
        description = in;
        return this;
    }

    public MultiValueStringMetric setPagePosition(String in) {
        position = in;
        return this;
    }
}
