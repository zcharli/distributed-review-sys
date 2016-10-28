package metric;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Created by cli on 10/28/2016.
 */
public class BaseMetric {

    @JsonIgnore
    private MetricType metricType;

    public String name;

    public String component;

    public String id;

    public static final String type = "metric";
    
    public BaseMetric() { }

    public BaseMetric setId(String id) {
        this.id = id;
        return this;
    }

    public BaseMetric setName(String name) {
        this.name = name;
        return this;
    }

    public BaseMetric setMetricType(MetricType metricType) {
        this.metricType = metricType;
        return this;
    }
}