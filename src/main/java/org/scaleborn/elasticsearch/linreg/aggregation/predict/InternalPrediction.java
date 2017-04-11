/*
 * Copyright (c) 2017 Scaleborn UG, www.scaleborn.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.scaleborn.elasticsearch.linreg.aggregation.predict;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.search.aggregations.pipeline.PipelineAggregator;
import org.scaleborn.elasticsearch.linreg.aggregation.support.BaseInternalAggregation;
import org.scaleborn.linereg.evaluation.SlopeCoefficients;

/**
 * Created by mbok on 11.04.17.
 */
public class InternalPrediction extends
    BaseInternalAggregation<PredictionSampling, PredictionResults, InternalPrediction> implements
    Prediction {

  protected InternalPrediction(final String name, final int featuresCount,
      final PredictionSampling sampling,
      final PredictionResults results,
      final List<PipelineAggregator> pipelineAggregators,
      final Map<String, Object> metaData) {
    super(name, featuresCount, sampling, results, pipelineAggregators, metaData);
  }

  public InternalPrediction(final StreamInput in) throws IOException {
    super(in, PredictionResults::new);
  }

  @Override
  public double getValue() {
    if (this.results == null) {
      return Double.NaN;
    }
    return this.results.getPredictedValue();
  }

  @Override
  protected PredictionSampling buildSampling(final int featuresCount) {
    return PredictionAggregationBuilder.buildSampling(featuresCount);
  }

  @Override
  protected Object getDoProperty(final String path) {
    if ("value".equals(path)) {
      return getValue();
    }
    return null;
  }

  @Override
  protected InternalPrediction buildInternalAggregation(final String name, final int featuresCount,
      final PredictionSampling linRegSampling, final PredictionResults results,
      final List<PipelineAggregator> pipelineAggregators, final Map<String, Object> metaData) {
    return new InternalPrediction(name, featuresCount, linRegSampling, results, pipelineAggregators,
        metaData);
  }

  @Override
  protected PredictionResults buildResults(final PredictionSampling composedSampling,
      final SlopeCoefficients slopeCoefficients) {
    // TODO calculate predicated value
    return new PredictionResults(2, slopeCoefficients);
  }

  @Override
  public String getWriteableName() {
    return PredictionAggregationBuilder.NAME;
  }
}
