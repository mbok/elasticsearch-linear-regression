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
import java.util.List;
import java.util.Map;
import org.elasticsearch.search.MultiValueMode;
import org.elasticsearch.search.aggregations.Aggregator;
import org.elasticsearch.search.aggregations.AggregatorFactories;
import org.elasticsearch.search.aggregations.AggregatorFactory;
import org.elasticsearch.search.aggregations.pipeline.PipelineAggregator;
import org.elasticsearch.search.aggregations.support.MultiValuesSourceAggregatorFactory;
import org.elasticsearch.search.aggregations.support.NamedValuesSourceConfigSpec;
import org.elasticsearch.search.aggregations.support.NamedValuesSourceSpec;
import org.elasticsearch.search.aggregations.support.ValuesSource.Numeric;
import org.elasticsearch.search.internal.SearchContext;

/**
 * Created by mbok on 29.03.17.
 */
public class StatsAggregatorFactory extends
    MultiValuesSourceAggregatorFactory<Numeric, StatsAggregatorFactory> {

  private final MultiValueMode multiValueMode;

  public StatsAggregatorFactory(String name,
      List<NamedValuesSourceConfigSpec<Numeric>> configs, MultiValueMode multiValueMode,
      SearchContext context, AggregatorFactory<?> parent,
      AggregatorFactories.Builder subFactoriesBuilder,
      Map<String, Object> metaData) throws IOException {
    super(name, configs, context, parent, subFactoriesBuilder, metaData);
    this.multiValueMode = multiValueMode;
  }

  @Override
  protected Aggregator createUnmapped(Aggregator parent,
      List<PipelineAggregator> pipelineAggregators, Map<String, Object> metaData)
      throws IOException {
    return new StatsAggregator(name, null, context, parent, multiValueMode,
        pipelineAggregators, metaData);
  }

  @Override
  protected Aggregator doCreateInternal(List<NamedValuesSourceSpec<Numeric>> valuesSources,
      Aggregator parent,
      boolean collectsFromSingleBucket, List<PipelineAggregator> pipelineAggregators,
      Map<String, Object> metaData) throws IOException {
    return new StatsAggregator(name, valuesSources, context, parent, multiValueMode,
        pipelineAggregators, metaData);
  }
}
