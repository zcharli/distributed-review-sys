package metric;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by cli on 10/28/2016.
 */
public class MultiValueMetric extends BaseMetric {

    public List<String> categories;
    public List<Integer> data;

    public MultiValueMetric() {
        categories = new LinkedList<>();
        data = new LinkedList<>();
    }

    public MultiValueMetric setCategories(List<String> in) {
        this.categories = in;
        return this;
    }

    public MultiValueMetric setData(List<Integer> in) {
        this.data = in;
        return this;
    }

    public MultiValueMetric addCategories(String in) {
        this.categories.add(in);
        return this;
    }

    public MultiValueMetric addData(Integer in) {
        this.data.add(in);
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
