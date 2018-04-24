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
import java.util.Map;
import java.util.Objects;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.search.MultiValueMode;
import org.elasticsearch.search.aggregations.AggregatorFactories;
import org.elasticsearch.search.aggregations.AggregatorFactory;
import org.elasticsearch.search.aggregations.support.MultiValuesSourceAggregatorFactory;
import org.elasticsearch.search.aggregations.support.ValuesSource;
import org.elasticsearch.search.aggregations.support.ValuesSource.Numeric;
import org.elasticsearch.search.aggregations.support.ValuesSourceConfig;
import org.elasticsearch.search.internal.SearchContext;
import org.scaleborn.elasticsearch.linreg.aggregation.support.BaseAggregationBuilder;
import org.scaleborn.linereg.sampling.exact.ExactModelSamplingFactory;
import org.scaleborn.linereg.sampling.exact.ExactSamplingContext;

/**
 * Created by mbok on 11.04.17.
 */
public class PredictionAggregationBuilder extends
    BaseAggregationBuilder<PredictionAggregationBuilder> {

  public static final String NAME = "linreg_predict";

  private static final ExactModelSamplingFactory MODEL_SAMPLING_FACTORY = new ExactModelSamplingFactory();
  private double[] inputs;

  public PredictionAggregationBuilder(final String name) {
    super(name);
  }

  public PredictionAggregationBuilder(final StreamInput in)
      throws IOException {
    super(in);
    this.inputs = in.readDoubleArray();
  }

  @Override
  protected void innerWriteTo(final StreamOutput out) throws IOException {
    super.innerWriteTo(out);
    out.writeDoubleArray(this.inputs);
  }

  @Override
  protected MultiValuesSourceAggregatorFactory<ValuesSource.Numeric, ?> innerInnerBuild(
      final SearchContext context,
      final Map<String, ValuesSourceConfig<Numeric>> configs, final MultiValueMode multiValueMode,
      final AggregatorFactory<?> parent, final AggregatorFactories.Builder subFactoriesBuilder)
      throws IOException {
    if (this.inputs == null || this.inputs.length != configs.size() - 1) {
      throw new IllegalArgumentException(
          "[inputs] must have [" + (configs.size() - 1)
              + "] values as much as the number of feature fields: ["
              + this.name
              + "]");
    }
    return new PredictionAggregatorFactory(this.name, configs, multiValueMode, this.inputs,
        context,
        parent,
        subFactoriesBuilder, this.metaData);
  }

  @Override
  public String getType() {
    return NAME;
  }

  static PredictionSampling buildSampling(final int featuresCount) {
    final ExactSamplingContext context = MODEL_SAMPLING_FACTORY
        .createContext(featuresCount);
    final PredictionSampling predictionSampling = new PredictionSampling(context,
        MODEL_SAMPLING_FACTORY.createCoefficientLinearTermSampling(context),
        MODEL_SAMPLING_FACTORY.createCoefficientSquareTermSampling(context),
        MODEL_SAMPLING_FACTORY.createInterceptSampling(context));
    return predictionSampling;
  }

  public void inputs(final double[] inputs) {
    this.inputs = inputs;
  }

  public double[] inputs() {
    return this.inputs;
  }

  @Override
  protected int innerHashCode() {
    return Objects.hash(this.inputs);
  }

  @Override
  protected boolean innerEquals(final Object obj) {
    final PredictionAggregationBuilder other = (PredictionAggregationBuilder) obj;
    return Objects.equals(this.inputs, other.inputs);
  }
}
