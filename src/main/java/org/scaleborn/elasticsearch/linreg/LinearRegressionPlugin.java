package org.scaleborn.elasticsearch.linreg;

import java.util.Arrays;
import java.util.List;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.plugins.SearchPlugin;
import org.scaleborn.elasticsearch.linreg.aggregation.predict.InternalPrediction;
import org.scaleborn.elasticsearch.linreg.aggregation.predict.PredictAggregationBuilder;

public class LinearRegressionPlugin extends Plugin implements SearchPlugin {

  @Override
  public List<AggregationSpec> getAggregations() {
    AggregationSpec predict = new AggregationSpec(PredictAggregationBuilder.NAME,
        PredictAggregationBuilder::new, PredictAggregationBuilder::parse)
        .addResultReader(InternalPrediction::new);
    return Arrays.asList(predict);
  }

  @Override
  public List<PipelineAggregationSpec> getPipelineAggregations() {
    // TODO Auto-generated method stub
    return SearchPlugin.super.getPipelineAggregations();
  }

}
