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

package org.scaleborn.elasticsearch.linreg.aggregation.support;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.elasticsearch.search.aggregations.Aggregator;
import org.elasticsearch.search.aggregations.AggregatorFactories;
import org.elasticsearch.search.aggregations.AggregatorFactory;
import org.elasticsearch.search.aggregations.pipeline.PipelineAggregator;
import org.elasticsearch.search.aggregations.support.ValuesSource.Numeric;
import org.elasticsearch.search.aggregations.support.ValuesSourceConfig;
import org.elasticsearch.search.internal.SearchContext;

/**
 * Created by mbok on 22.03.17.
 */
public abstract class BaseAggregatorFactory<AF extends BaseAggregatorFactory<AF>> extends
    AggregatorFactory<AF> {

  private final List<ValuesSourceConfig<Numeric>> featureConfigs;
  private final ValuesSourceConfig<Numeric> responseConfig;

  public BaseAggregatorFactory(final String name,
      final List<ValuesSourceConfig<Numeric>> featureConfigs,
      final ValuesSourceConfig<Numeric> responseConfig,
      final SearchContext context,
      final AggregatorFactory<?> parent, final AggregatorFactories.Builder subFactoriesBuilder,
      final Map<String, Object> metaData) throws IOException {
    super(name, context, parent, subFactoriesBuilder, metaData);
    this.featureConfigs = featureConfigs;
    this.responseConfig = responseConfig;
  }

  @Override
  public Aggregator createInternal(final Aggregator parent, final boolean collectsFromSingleBucket,
      final List<PipelineAggregator> pipelineAggregators, final Map<String, Object> metaData)
      throws IOException {
    final List<Numeric> featuresValuesSources = new ArrayList<>(this.featureConfigs.size());
    for (final ValuesSourceConfig<Numeric> featureConfig : this.featureConfigs) {
      Numeric source = featureConfig.toValuesSource(this.context.getQueryShardContext());
      if (source == null) {
        source = Numeric.EMPTY;
      }
      featuresValuesSources.add(source);
    }
    Numeric responseSource = this.responseConfig
        .toValuesSource(this.context.getQueryShardContext());
    if (responseSource == null) {
      responseSource = Numeric.EMPTY;
    }
    return doCreateInternal(featuresValuesSources, responseSource, parent, collectsFromSingleBucket,
        pipelineAggregators, metaData);
  }

  protected abstract Aggregator doCreateInternal(List<Numeric> featuresValuesSources,
      Numeric responseValuesSource, Aggregator parent,
      boolean collectsFromSingleBucket, List<PipelineAggregator> pipelineAggregators,
      Map<String, Object> metaData)
      throws IOException;


}
