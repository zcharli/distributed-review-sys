package review.response.metric;

import com.fasterxml.jackson.annotation.JsonProperty;
import metric.BaseMetric;
import review.response.BaseRestWrapper;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Created by cli on 10/28/2016.
 */
public class MetricPayloadRestWrapper extends BaseRestWrapper {

    @JsonProperty("type")
    public final String type = "metrics";

    @Nullable
    public Collection<BaseMetric> metrics;

    public MetricPayloadRestWrapper() {}


    public MetricPayloadRestWrapper setMetrics(Collection<BaseMetric> metrics) {
        if (this.metrics == null) {
            this.metrics = metrics;
        } else {
            this.metrics.addAll(metrics);
        }
        return this;
    }
    public MetricPayloadRestWrapper add(BaseMetric metric) {
        if (this.metrics == null) {
            this.metrics = new LinkedList<>();
        }
        metrics.add(metric);
        return this;
    }
}
