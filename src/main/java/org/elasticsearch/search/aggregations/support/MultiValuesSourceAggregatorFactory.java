/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.search.aggregations.support;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.elasticsearch.search.aggregations.Aggregator;
import org.elasticsearch.search.aggregations.AggregatorFactories;
import org.elasticsearch.search.aggregations.AggregatorFactory;
import org.elasticsearch.search.aggregations.pipeline.PipelineAggregator;
import org.elasticsearch.search.internal.SearchContext;

public abstract class MultiValuesSourceAggregatorFactory<VS extends ValuesSource, AF extends MultiValuesSourceAggregatorFactory<VS, AF>>
    extends AggregatorFactory<AF> {

  protected Map<String, ValuesSourceConfig<VS>> configs;

  public MultiValuesSourceAggregatorFactory(final String name,
      final Map<String, ValuesSourceConfig<VS>> configs,
      final SearchContext context, final AggregatorFactory<?> parent,
      final AggregatorFactories.Builder subFactoriesBuilder,
      final Map<String, Object> metaData) throws IOException {
    super(name, context, parent, subFactoriesBuilder, metaData);
    this.configs = configs;
  }

  @Override
  public Aggregator createInternal(final Aggregator parent, final boolean collectsFromSingleBucket,
      final List<PipelineAggregator> pipelineAggregators,
      final Map<String, Object> metaData) throws IOException {
    final HashMap<String, VS> valuesSources = new LinkedHashMap<>();

    for (final Map.Entry<String, ValuesSourceConfig<VS>> config : this.configs.entrySet()) {
      final VS vs = config.getValue().toValuesSource(this.context.getQueryShardContext());
      if (vs != null) {
        valuesSources.put(config.getKey(), vs);
      }
    }
    if (valuesSources.isEmpty()) {
      return createUnmapped(parent, pipelineAggregators, metaData);
    }
    return doCreateInternal(valuesSources, parent, collectsFromSingleBucket, pipelineAggregators,
        metaData);
  }

  protected abstract Aggregator createUnmapped(Aggregator parent,
      List<PipelineAggregator> pipelineAggregators,
      Map<String, Object> metaData) throws IOException;

  protected abstract Aggregator doCreateInternal(Map<String, VS> valuesSources, Aggregator parent,
      boolean collectsFromSingleBucket,
      List<PipelineAggregator> pipelineAggregators, Map<String, Object> metaData)
      throws IOException;

}