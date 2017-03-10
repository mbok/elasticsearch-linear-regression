package org.scaleborn.elasticsearch.linreg.aggregation.predict;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.search.aggregations.InternalAggregation;
import org.elasticsearch.search.aggregations.metrics.InternalNumericMetricsAggregation;
import org.elasticsearch.search.aggregations.pipeline.PipelineAggregator;

/**
 * Created by mbok on 07.03.17.
 */
public class InternalPrediction extends InternalNumericMetricsAggregation.MultiValue implements
    Prediction {

    private final long count;

    public InternalPrediction(String name, long count,
        List<PipelineAggregator> pipelineAggregators,
        Map<String, Object> metaData) {
        super(name, pipelineAggregators, metaData);
        this.count = count;
    }

    public InternalPrediction(StreamInput in) throws IOException {
        super(in);
        count = in.readLong();
    }

    @Override
    public double value(String name) {
        return 0;
    }

    @Override
    protected void doWriteTo(StreamOutput out) throws IOException {
        out.writeLong(count);
    }

    @Override
    public InternalAggregation doReduce(List<InternalAggregation> aggregations,
        ReduceContext reduceContext) {
        long count = 0;
        for (InternalAggregation aggregation : aggregations) {
            count += ((InternalPrediction) aggregation).count;
        }
        return new InternalPrediction(getName(), count, pipelineAggregators(), getMetaData());
    }

    @Override
    public XContentBuilder doXContentBody(XContentBuilder builder, Params params)
        throws IOException {
        builder.field(Fields.PREDICTION, getPrediction());
        return builder;
    }

    @Override
    public String getWriteableName() {
        return PredictAggregationBuilder.NAME;
    }

    @Override
    public double getPrediction() {
        return count / 2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof InternalPrediction)) {
            return false;
        }

        InternalPrediction that = (InternalPrediction) o;

        return count == that.count;
    }

    @Override
    public int hashCode() {
        return (int) (count ^ (count >>> 32));
    }

    static class Fields {

        public static final String PREDICTION = "prediction";
    }
}
