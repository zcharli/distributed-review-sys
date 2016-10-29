package metric;

/**
 * Created by cli on 10/29/2016.
 */
public class MultiValueLongMetric extends BaseMetric {

    public String[] labels;
    public long[] values;

    public MultiValueLongMetric() { }

    public MultiValueLongMetric setLabels(String[] in) {
        this.labels = in;
        return this;
    }

    public MultiValueLongMetric setData(long[] in) {
        this.values = in;
        return this;
    }

    public MultiValueLongMetric setId(String id) {
        this.id = id;
        return this;
    }

    public MultiValueLongMetric setName(String name) {
        this.name = name;
        return this;
    }

    public MultiValueLongMetric setMetricType(MetricType metricType) {
        super.setMetricType(metricType);
        return this;
    }

    public MultiValueLongMetric setComponent(String in) {
        component = in;
        return this;
    }

    public MultiValueLongMetric setIcon(String in) {
        icon = in;
        return this;
    }

    public MultiValueLongMetric setDescription(String in) {
        description = in;
        return this;
    }

    public MultiValueLongMetric setPagePosition(String in) {
        position = in;
        return this;
    }
}
