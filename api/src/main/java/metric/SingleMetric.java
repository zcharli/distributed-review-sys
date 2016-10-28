package metric;

/**
 * Created by cli on 10/28/2016.
 */
public class SingleMetric extends BaseMetric {

    public String value;

    public SingleMetric() {}

    public SingleMetric setId(String id) {
        this.id = id;
        return this;
    }

    public SingleMetric setValue(String value) {
        this.value = value;
        return this;
    }

    public SingleMetric setName(String name) {
        this.name = name;
        return this;
    }

    public SingleMetric setMetricType(MetricType metricType) {
        super.setMetricType(metricType);
        return this;
    }

    public SingleMetric setComponent(String in) {
        component = in;
        return this;
    }

    public SingleMetric setIcon(String in) {
        icon = in;
        return this;
    }

    public SingleMetric setDescription(String in) {
        description = in;
        return this;
    }

    public SingleMetric setPagePosition(String in) {
        position = in;
        return this;
    }
}
