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
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.search.MultiValueMode;
import org.elasticsearch.search.aggregations.AggregatorFactories;
import org.elasticsearch.search.aggregations.AggregatorFactory;
import org.elasticsearch.search.aggregations.support.MultiValuesSourceAggregationBuilder;
import org.elasticsearch.search.aggregations.support.MultiValuesSourceAggregatorFactory;
import org.elasticsearch.search.aggregations.support.ValueType;
import org.elasticsearch.search.aggregations.support.ValuesSource;
import org.elasticsearch.search.aggregations.support.ValuesSource.Numeric;
import org.elasticsearch.search.aggregations.support.ValuesSourceConfig;
import org.elasticsearch.search.aggregations.support.ValuesSourceType;
import org.elasticsearch.search.internal.SearchContext;

/**
 * Created by mbok on 22.03.17.
 */
public abstract class BaseAggregationBuilder<S extends BaseAggregationBuilder<S>> extends
    MultiValuesSourceAggregationBuilder.LeafOnly<ValuesSource.Numeric, S> {

  private static final Logger LOGGER = Loggers.getLogger(BaseAggregationBuilder.class);

  private MultiValueMode multiValueMode = MultiValueMode.AVG;

  public S multiValueMode(final MultiValueMode multiValueMode) {
    this.multiValueMode = multiValueMode;
    //noinspection unchecked
    return (S) this;
  }

  public MultiValueMode multiValueMode() {
    return this.multiValueMode;
  }

  public BaseAggregationBuilder(final String name) {
    super(name, ValuesSourceType.NUMERIC, ValueType.NUMERIC);
  }

  /**
   * Read from a stream.
   */
  public BaseAggregationBuilder(final StreamInput in) throws IOException {
    super(in, ValuesSourceType.NUMERIC, ValueType.NUMERIC);
  }

  @Override
  protected MultiValuesSourceAggregatorFactory<ValuesSource.Numeric, ?> innerBuild(
      final SearchContext context,
      final Map<String, ValuesSourceConfig<Numeric>> configs, final AggregatorFactory<?> parent,
      final AggregatorFactories.Builder subFactoriesBuilder) throws IOException {
    return innerInnerBuild(context, configs, this.multiValueMode, parent, subFactoriesBuilder);
  }

  protected abstract MultiValuesSourceAggregatorFactory<ValuesSource.Numeric, ?> innerInnerBuild(
      SearchContext context,
      Map<String, ValuesSourceConfig<Numeric>> configs, MultiValueMode multiValueMode,
      AggregatorFactory<?> parent, AggregatorFactories.Builder subFactoriesBuilder)
      throws IOException;

  @Override
  public XContentBuilder doXContentBody(final XContentBuilder builder,
      final ToXContent.Params params)
      throws IOException {
    builder.field(MULTIVALUE_MODE_FIELD.getPreferredName(), this.multiValueMode);
    return builder;
  }

  @SuppressWarnings("unchecked")
  @Override
  public S fields(final List<String> fields) {
    super.fields(fields);
    LOGGER.debug("Setting fields for aggregation: {}", fields);
    if (fields.size() < 2) {
      throw new IllegalArgumentException(
          "[fields] must reference at least two fields (multiple features and the response as the last field): ["
              + this.name
              + "]");
    }
    return (S) this;
  }

  @Override
  protected void innerWriteTo(final StreamOutput out) throws IOException {
    // Do nothing, no extra state to write to stream
  }

  @Override
  protected int innerHashCode() {
    return 0;
  }

  @Override
  protected boolean innerEquals(final Object obj) {
    return true;
  }
}
