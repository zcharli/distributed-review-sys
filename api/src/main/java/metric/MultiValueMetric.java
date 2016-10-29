package metric;


import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Created by cli on 10/28/2016.
 */
public class MultiValueMetric extends BaseMetric {

    public String[] labels;
    public int[] data;

    public MultiValueMetric() { }

    public MultiValueMetric setLabels(String[] in) {
        this.labels = in;
        return this;
    }

    public MultiValueMetric setData(int[] in) {
        this.data = in;
        return this;
    }

    public MultiValueMetric setId(String id) {
        this.id = id;
        return this;
    }

    public MultiValueMetric setName(String name) {
        this.name = name;
        return this;
    }

    public MultiValueMetric setMetricType(MetricType metricType) {
        super.setMetricType(metricType);
        return this;
    }

    public MultiValueMetric setComponent(String in) {
        component = in;
        return this;
    }

    public MultiValueMetric setIcon(String in) {
        icon = in;
        return this;
    }

    public MultiValueMetric setDescription(String in) {
        description = in;
        return this;
    }

    public MultiValueMetric setPagePosition(String in) {
        position = in;
        return this;
    }
}
