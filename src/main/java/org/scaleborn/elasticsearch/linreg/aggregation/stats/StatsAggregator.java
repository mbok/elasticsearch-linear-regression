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
import org.apache.logging.log4j.Logger;
import org.apache.lucene.index.LeafReaderContext;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.util.BigArrays;
import org.elasticsearch.common.util.ObjectArray;
import org.elasticsearch.index.fielddata.NumericDoubleValues;
import org.elasticsearch.search.MultiValueMode;
import org.elasticsearch.search.aggregations.Aggregator;
import org.elasticsearch.search.aggregations.InternalAggregation;
import org.elasticsearch.search.aggregations.LeafBucketCollector;
import org.elasticsearch.search.aggregations.LeafBucketCollectorBase;
import org.elasticsearch.search.aggregations.metrics.MetricsAggregator;
import org.elasticsearch.search.aggregations.pipeline.PipelineAggregator;
import org.elasticsearch.search.aggregations.support.MultiValuesSource.NumericMultiValuesSource;
import org.elasticsearch.search.aggregations.support.NamedValuesSourceSpec;
import org.elasticsearch.search.aggregations.support.ValuesSource.Numeric;
import org.elasticsearch.search.internal.SearchContext;
import org.scaleborn.linereg.statistics.StatsSampling;

/**
 * Created by mbok on 21.03.17.
 */
public class StatsAggregator extends MetricsAggregator {

  private static final Logger LOGGER = Loggers.getLogger(StatsAggregator.class);

  /**
   * Multiple ValuesSource with field names
   */
  final NumericMultiValuesSource valuesSources;

  protected ObjectArray<StatsSampling<?>> samplings;

  public StatsAggregator(String name, List<NamedValuesSourceSpec<Numeric>> valuesSources,
      SearchContext context,
      Aggregator parent, MultiValueMode multiValueMode,
      List<PipelineAggregator> pipelineAggregators,
      Map<String, Object> metaData) throws IOException {
    super(name, context, parent, pipelineAggregators, metaData);
    if (valuesSources != null && !valuesSources.isEmpty()) {
      this.valuesSources = new NumericMultiValuesSource(valuesSources, multiValueMode);
      samplings = context.bigArrays().newObjectArray(1);
    } else {
      this.valuesSources = null;
    }
  }

  @Override
  public boolean needsScores() {
    return (valuesSources == null) ? false : valuesSources.needsScores();
  }

  @Override
  public LeafBucketCollector getLeafCollector(LeafReaderContext ctx,
      final LeafBucketCollector sub) throws IOException {
    if (valuesSources == null) {
      return LeafBucketCollector.NO_OP_COLLECTOR;
    }
    final BigArrays bigArrays = context.bigArrays();
    final NumericDoubleValues[] values = new NumericDoubleValues[valuesSources.fieldNames().length];
    for (int i = 0; i < values.length; ++i) {
      values[i] = valuesSources.getField(i, ctx);
    }

    return new LeafBucketCollectorBase(sub, values) {
      final String[] fieldNames = valuesSources.fieldNames();
      final double[] fieldVals = new double[fieldNames.length];

      @Override
      public void collect(int doc, long bucket) throws IOException {
        // get fields
        if (includeDocument(doc) == true) {
          samplings = bigArrays.grow(samplings, bucket + 1);
          StatsSampling<?> sampling = samplings.get(bucket);
          // add document fields to correlation stats
          if (sampling == null) {
            sampling = StatsAggregationBuilder.buildSampling(fieldNames.length - 1);
            samplings.set(bucket, sampling);
          }
          LOGGER.info("Sampling for bucket={}, fields={}", bucket, fieldVals);
          sampling.sample(fieldVals, fieldVals[fieldVals.length - 1]);
        } else {
          LOGGER.warn("Skipped bucket={}, fields={}", bucket, fieldVals);
        }
      }

      /**
       * return a map of field names and data
       */
      private boolean includeDocument(int doc) {
        // loop over fields
        for (int i = 0; i < fieldVals.length; ++i) {
          final NumericDoubleValues doubleValues = values[i];
          final double value = doubleValues.get(doc);
          // skip if value is missing
          if (value == Double.NEGATIVE_INFINITY) {
            return false;
          }
          fieldVals[i] = value;
        }
        return true;
      }
    };
  }


  @Override
  public InternalAggregation buildAggregation(long bucket) {
    if (valuesSources == null || bucket >= samplings.size()) {
      return buildEmptyAggregation();
    }
    return new InternalStats(name, valuesSources.fieldNames().length - 1,
        samplings.get(bucket), null,
        pipelineAggregators(), metaData());
  }

  @Override
  public InternalAggregation buildEmptyAggregation() {
    return new InternalStats(name, 0, null, null, pipelineAggregators(), metaData());
  }

  @Override
  public void doClose() {
    // Releasables.close(stats);
  }
}
