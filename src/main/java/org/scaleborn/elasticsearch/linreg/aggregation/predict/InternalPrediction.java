package org.scaleborn.elasticsearch.linreg.aggregation.predict;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.search.aggregations.InternalAggregation;
import org.elasticsearch.search.aggregations.metrics.InternalNumericMetricsAggregation;
import org.elasticsearch.search.aggregations.metrics.stats.Stats;
import org.elasticsearch.search.aggregations.pipeline.PipelineAggregator;

/**
 * Created by mbok on 07.03.17.
 */
public class InternalPrediction extends InternalNumericMetricsAggregation.MultiValue implements
    Prediction {


  public InternalPrediction(StreamInput in) throws IOException {
    super(in);
  }

  @Override
  public double value(String name) {
    return 0;
  }

  @Override
  protected void doWriteTo(StreamOutput out) throws IOException {

  }

  @Override
  public InternalAggregation doReduce(List<InternalAggregation> aggregations,
      ReduceContext reduceContext) {
    return null;
  }

  @Override
  public XContentBuilder doXContentBody(XContentBuilder builder, Params params) throws IOException {
    return null;
  }

  @Override
  public String getWriteableName() {
    return null;
  }

  @Override
  public double getPrediction() {
    return 0;
  }
}
