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
import java.util.Objects;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.search.aggregations.pipeline.PipelineAggregator;
import org.scaleborn.elasticsearch.linreg.aggregation.support.BaseInternalAggregation;
import org.scaleborn.linereg.estimation.SlopeCoefficients;

/**
 * Created by mbok on 11.04.17.
 */
public class InternalPrediction extends
    BaseInternalAggregation<PredictionSampling, PredictionResults, InternalPrediction> implements
    Prediction {

  private static final Logger LOGGER = Loggers.getLogger(InternalPrediction.class);

  private final double[] inputs;

  protected InternalPrediction(final String name, final int featuresCount,
      final PredictionSampling sampling,
      final PredictionResults results,
      final double[] inputs, final List<PipelineAggregator> pipelineAggregators,
      final Map<String, Object> metaData) {
    super(name, featuresCount, sampling, results, pipelineAggregators, metaData);
    this.inputs = inputs;
  }

  public InternalPrediction(final StreamInput in) throws IOException {
    super(in, PredictionResults::new);
    this.inputs = in.readDoubleArray();
  }

  @Override
  protected void doWriteTo(final StreamOutput out) throws IOException {
    super.doWriteTo(out);
    out.writeDoubleArray(this.inputs);
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
    return new InternalPrediction(name, featuresCount, linRegSampling, results, this.inputs,
        pipelineAggregators,
        metaData);
  }

  @Override
  protected PredictionResults buildResults(final PredictionSampling composedSampling,
      final SlopeCoefficients slopeCoefficients, final double intercept) {
    double predictedValue = intercept;
    LOGGER.debug("Predicting values for inputs: {}", this.inputs);
    for (int i = 0; i < this.featuresCount; i++) {
      predictedValue += slopeCoefficients.getCoefficients()[i] * this.inputs[i];
    }
    return new PredictionResults(predictedValue, slopeCoefficients, intercept);
  }

  @Override
  public String getWriteableName() {
    return PredictionAggregationBuilder.NAME;
  }

  @Override
  protected int doHashCode() {
    return super.doHashCode() + Objects.hash(this.inputs);
  }

  @Override
  protected boolean doEquals(final Object obj) {
    final InternalPrediction other = (InternalPrediction) obj;
    return super.doEquals(obj) && Objects.equals(this.inputs, other.inputs);
  }
}
