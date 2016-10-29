package metric;

import com.fasterxml.jackson.annotation.JsonIgnore;
import net.tomp2p.peers.Number160;

/**
 * Created by cli on 10/28/2016.
 */
public abstract class BaseMetric {

    @JsonIgnore
    protected MetricType metricType;

    public String name; // MUST be unique

    public String component;

    public String id;

    public String position;

    public String description;

    public String icon;

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
        component = metricType.getComponent();
        icon = metricType.getIcon();
        name = metricType.getName();
        position = metricType.getPagePosition();
        description = metricType.getDescription();
        this.id = Number160.createHash(name).toString();
        return this;
    }

    public BaseMetric setComponent(String in) {
        component = in;
        return this;
    }

    public BaseMetric setIcon(String in) {
        icon = in;
        return this;
    }

    public BaseMetric setDescription(String in) {
        description = in;
        return this;
    }

    public BaseMetric setPagePosition(String in) {
        position = in;
        return this;
    }
}