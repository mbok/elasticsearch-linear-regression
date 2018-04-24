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

package org.scaleborn.elasticsearch.linreg.aggregation.stats;

import java.io.IOException;
import java.util.Map;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.search.MultiValueMode;
import org.elasticsearch.search.aggregations.AggregatorFactories;
import org.elasticsearch.search.aggregations.AggregatorFactory;
import org.elasticsearch.search.aggregations.support.MultiValuesSourceAggregatorFactory;
import org.elasticsearch.search.aggregations.support.ValuesSource.Numeric;
import org.elasticsearch.search.aggregations.support.ValuesSourceConfig;
import org.elasticsearch.search.internal.SearchContext;
import org.scaleborn.elasticsearch.linreg.aggregation.support.BaseAggregationBuilder;
import org.scaleborn.linereg.sampling.exact.ExactModelSamplingFactory;
import org.scaleborn.linereg.sampling.exact.ExactSamplingContext;

/**
 * Created by mbok on 21.03.17.
 */
public class StatsAggregationBuilder extends
    BaseAggregationBuilder<StatsAggregationBuilder> {

  public static final String NAME = "linreg_stats";

  private static final ExactModelSamplingFactory MODEL_SAMPLING_FACTORY = new ExactModelSamplingFactory();


  public StatsAggregationBuilder(final String name) {
    super(name);
  }

  public StatsAggregationBuilder(final StreamInput in) throws IOException {
    super(in);
  }

  static StatsAggregationSampling buildSampling(final int featuresCount) {
    final ExactSamplingContext context = MODEL_SAMPLING_FACTORY
        .createContext(featuresCount);
    final StatsAggregationSampling statsSampling = new StatsAggregationSampling(context,
        MODEL_SAMPLING_FACTORY.createCoefficientLinearTermSampling(context),
        MODEL_SAMPLING_FACTORY.createCoefficientSquareTermSampling(context),
        MODEL_SAMPLING_FACTORY.createInterceptSampling(context),
        MODEL_SAMPLING_FACTORY.createResponseVarianceTermSampling(context));
    return statsSampling;
  }

  @Override
  protected MultiValuesSourceAggregatorFactory<Numeric, ?> innerInnerBuild(
      final SearchContext context,
      final Map<String, ValuesSourceConfig<Numeric>> configs, final MultiValueMode multiValueMode,
      final AggregatorFactory<?> parent, final AggregatorFactories.Builder subFactoriesBuilder)
      throws IOException {
    return new StatsAggregatorFactory(this.name, configs, multiValueMode, context, parent,
        subFactoriesBuilder, this.metaData);
  }

  @Override
  public String getType() {
    return NAME;
  }
}