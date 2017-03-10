package org.scaleborn.elasticsearch.linreg.aggregation.predict;

import java.util.List;
import java.util.Map;
import org.elasticsearch.common.io.stream.Writeable.Reader;
import org.elasticsearch.search.aggregations.InternalAggregationTestCase;
import org.elasticsearch.search.aggregations.pipeline.PipelineAggregator;

/**
 * Created by mbok on 09.03.17.
 */
public class InternalPredictionTests extends InternalAggregationTestCase<InternalPrediction> {

  @Override
  protected InternalPrediction createTestInstance(String name,
      List<PipelineAggregator> pipelineAggregators, Map<String, Object> metaData) {
    return new InternalPrediction(name, 0, pipelineAggregators,
        metaData);
  }

  @Override
  protected void assertReduced(InternalPrediction reduced, List<InternalPrediction> inputs) {

  }

  @Override
  protected Reader<InternalPrediction> instanceReader() {
    return InternalPrediction::new;
  }
}
