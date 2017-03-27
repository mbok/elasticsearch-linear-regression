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
import org.apache.lucene.index.LeafReaderContext;
import org.elasticsearch.common.util.ObjectArray;
import org.elasticsearch.search.aggregations.Aggregator;
import org.elasticsearch.search.aggregations.InternalAggregation;
import org.elasticsearch.search.aggregations.LeafBucketCollector;
import org.elasticsearch.search.aggregations.metrics.NumericMetricsAggregator;
import org.elasticsearch.search.aggregations.pipeline.PipelineAggregator;
import org.elasticsearch.search.internal.SearchContext;
import org.scaleborn.linereg.sampling.Sampler;

/**
 * Created by mbok on 21.03.17.
 */
public class StatsAggregator extends NumericMetricsAggregator.MultiValue {

  protected ObjectArray<Sampler<?>> samplers;

  public StatsAggregator(final String name, final SearchContext context,
      final Aggregator parent,
      final List<PipelineAggregator> pipelineAggregators,
      final Map<String, Object> metaData,
      final ObjectArray<Sampler<?>> samples) throws IOException {
    super(name, context, parent, pipelineAggregators, metaData);
    this.samplers = samplers;
  }

  @Override
  public boolean hasMetric(final String name) {
    return false;
  }

  @Override
  public double metric(final String name, final long owningBucketOrd) {
    return 0;
  }

  @Override
  protected LeafBucketCollector getLeafCollector(final LeafReaderContext ctx,
      final LeafBucketCollector sub)
      throws IOException {
    return null;
  }

  @Override
  public InternalAggregation buildAggregation(final long bucket) throws IOException {
    return null;
  }

  @Override
  public InternalAggregation buildEmptyAggregation() {
    return null;
  }
}
