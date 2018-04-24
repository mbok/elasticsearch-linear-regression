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
import org.elasticsearch.search.aggregations.support.ValuesSource;
import org.elasticsearch.search.internal.SearchContext;

/**
 * Created by mbok on 11.04.17.
 */
public abstract class BaseSamplingAggregator<S extends BaseSampling<S>> extends MetricsAggregator {

  private static final Logger LOGGER = Loggers.getLogger(BaseSamplingAggregator.class);

  /**
   * Multiple ValuesSource with field names
   */
  protected final NumericMultiValuesSource valuesSources;

  protected ObjectArray<S> samplings;

  private int fieldsCount;

  public BaseSamplingAggregator(final String name,
      final Map<String, ValuesSource.Numeric> valuesSources,
      final SearchContext context,
      final Aggregator parent, final MultiValueMode multiValueMode,
      final List<PipelineAggregator> pipelineAggregators,
      final Map<String, Object> metaData) throws IOException {
    super(name, context, parent, pipelineAggregators, metaData);
    LOGGER.debug("Setting up aggregator for fields: {} ({})", valuesSources,
        valuesSources.getClass());
    if (valuesSources != null && !valuesSources.isEmpty()) {
      this.valuesSources = new NumericMultiValuesSource(valuesSources, multiValueMode);
      this.samplings = context.bigArrays().newObjectArray(1);
      this.fieldsCount = this.valuesSources.fieldNames().length;
    } else {
      this.valuesSources = null;
    }
  }

  @Override
  public boolean needsScores() {
    return this.valuesSources == null || this.valuesSources.needsScores();
  }

  @Override
  public LeafBucketCollector getLeafCollector(final LeafReaderContext ctx,
      final LeafBucketCollector sub) throws IOException {
    if (this.valuesSources == null) {
      return LeafBucketCollector.NO_OP_COLLECTOR;
    }
    final BigArrays bigArrays = this.context.bigArrays();
    final NumericDoubleValues[] values = new NumericDoubleValues[this.fieldsCount];
    for (int i = 0; i < this.fieldsCount; ++i) {
      values[i] = this.valuesSources.getField(i, ctx);
    }
    LOGGER.debug("Starting sampling on fields: {}", this.valuesSources.fieldNames());
    return new LeafBucketCollectorBase(sub, values) {
      final double[] fieldVals = new double[BaseSamplingAggregator.this.fieldsCount];

      @Override
      public void collect(final int doc, final long bucket) throws IOException {
        // get fields
        if (includeDocument(doc) == true) {
          BaseSamplingAggregator.this.samplings = bigArrays
              .grow(BaseSamplingAggregator.this.samplings, bucket + 1);
          S sampling = BaseSamplingAggregator.this.samplings.get(bucket);
          // add document fields to correlation stats
          if (sampling == null) {
            sampling = buildSampling(BaseSamplingAggregator.this.fieldsCount - 1);
            BaseSamplingAggregator.this.samplings.set(bucket, sampling);
          }
          // LOGGER.info("Sampling for bucket={}, fields={}", bucket, this.fieldVals);
          sampling
              .sample(this.fieldVals, this.fieldVals[BaseSamplingAggregator.this.fieldsCount - 1]);
        } else {
          // LOGGER.warn("Skipped bucket={}, fields={}", bucket, this.fieldVals);
        }
      }

      /**
       * return a map of field names and data
       */
      private boolean includeDocument(final int doc) throws IOException {
        // loop over fields
        for (int i = 0; i < BaseSamplingAggregator.this.fieldsCount; ++i) {
          final NumericDoubleValues doubleValues = values[i];
          if (doubleValues.advanceExact(doc)) {
            final double value = doubleValues.doubleValue();
            if (value == Double.NEGATIVE_INFINITY) {
              return false;
            }
            this.fieldVals[i] = value;
          } else {
            return false;
          }
        }
        return true;
      }
    };
  }

  protected abstract S buildSampling(final int featuresCount);


  @Override
  public InternalAggregation buildAggregation(final long bucket) {
    if (this.valuesSources == null || bucket >= this.samplings.size()) {
      return buildEmptyAggregation();
    }
    return doBuildAggregation(this.name, this.valuesSources.fieldNames().length - 1,
        this.samplings.get(bucket), pipelineAggregators(), metaData());
  }

  protected abstract InternalAggregation doBuildAggregation(final String name,
      final int featuresCount,
      final S s, final List<PipelineAggregator> pipelineAggregators,
      final Map<String, Object> stringObjectMap);


  @Override
  public void doClose() {
    // Releasables.close(stats);
  }
}
