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
import org.elasticsearch.search.MultiValueMode;
import org.elasticsearch.search.aggregations.Aggregator;
import org.elasticsearch.search.aggregations.InternalAggregation;
import org.elasticsearch.search.aggregations.pipeline.PipelineAggregator;
import org.elasticsearch.search.aggregations.support.ValuesSource;
import org.elasticsearch.search.internal.SearchContext;
import org.scaleborn.elasticsearch.linreg.aggregation.support.BaseSamplingAggregator;

/**
 * Created by mbok on 11.04.17.
 */
public class PredictionAggregator extends BaseSamplingAggregator<PredictionSampling> {

  private final double[] inputs;

  public PredictionAggregator(final String name,
      final Map<String, ValuesSource.Numeric> valuesSources,
      final SearchContext context,
      final Aggregator parent,
      final MultiValueMode multiValueMode,
      final double[] inputs, final List<PipelineAggregator> pipelineAggregators,
      final Map<String, Object> metaData) throws IOException {
    super(name, valuesSources, context, parent, multiValueMode, pipelineAggregators, metaData);
    this.inputs = inputs;
  }

  @Override
  protected PredictionSampling buildSampling(final int featuresCount) {
    return PredictionAggregationBuilder.buildSampling(featuresCount);
  }

  @Override
  protected InternalAggregation doBuildAggregation(final String name, final int featuresCount,
      final PredictionSampling predictionSampling,
      final List<PipelineAggregator> pipelineAggregators,
      final Map<String, Object> stringObjectMap) {
    return new InternalPrediction(this.name, this.valuesSources.fieldNames().length - 1,
        predictionSampling, null, this.inputs,
        pipelineAggregators(), metaData());
  }

  @Override
  public InternalAggregation buildEmptyAggregation() {
    return new InternalPrediction(this.name, 0, null, null, this.inputs, pipelineAggregators(),
        metaData());
  }
}
