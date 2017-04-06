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
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.search.MultiValueMode;
import org.elasticsearch.search.aggregations.AggregatorFactories;
import org.elasticsearch.search.aggregations.AggregatorFactory;
import org.elasticsearch.search.aggregations.support.MultiValuesSourceAggregationBuilder;
import org.elasticsearch.search.aggregations.support.MultiValuesSourceAggregatorFactory;
import org.elasticsearch.search.aggregations.support.NamedValuesSourceConfigSpec;
import org.elasticsearch.search.aggregations.support.ValueType;
import org.elasticsearch.search.aggregations.support.ValuesSource;
import org.elasticsearch.search.aggregations.support.ValuesSource.Numeric;
import org.elasticsearch.search.aggregations.support.ValuesSourceType;
import org.elasticsearch.search.internal.SearchContext;

/**
 * Created by mbok on 22.03.17.
 */
public abstract class BaseAggregationBuilder<S extends BaseAggregationBuilder<S>> extends
    MultiValuesSourceAggregationBuilder.LeafOnly<ValuesSource.Numeric, S> {

  private MultiValueMode multiValueMode = MultiValueMode.AVG;

  public S multiValueMode(MultiValueMode multiValueMode) {
    this.multiValueMode = multiValueMode;
    //noinspection unchecked
    return (S) this;
  }

  public MultiValueMode multiValueMode() {
    return this.multiValueMode;
  }

  public BaseAggregationBuilder(String name) {
    super(name, ValuesSourceType.NUMERIC, ValueType.NUMERIC);
  }

  /**
   * Read from a stream.
   */
  public BaseAggregationBuilder(StreamInput in) throws IOException {
    super(in, ValuesSourceType.NUMERIC, ValueType.NUMERIC);
  }

  @Override
  protected final MultiValuesSourceAggregatorFactory<ValuesSource.Numeric, ?> innerBuild(
      SearchContext context,
      List<NamedValuesSourceConfigSpec<Numeric>> configs,
      AggregatorFactory<?> parent, AggregatorFactories.Builder subFactoriesBuilder)
      throws IOException {
    return innerInnerBuild(context, configs, multiValueMode, parent, subFactoriesBuilder);
  }

  protected abstract MultiValuesSourceAggregatorFactory<ValuesSource.Numeric, ?> innerInnerBuild(
      SearchContext context,
      List<NamedValuesSourceConfigSpec<Numeric>> configs, MultiValueMode multiValueMode,
      AggregatorFactory<?> parent, AggregatorFactories.Builder subFactoriesBuilder)
      throws IOException;

  @Override
  public XContentBuilder doXContentBody(XContentBuilder builder, ToXContent.Params params)
      throws IOException {
    builder.field(MULTIVALUE_MODE_FIELD.getPreferredName(), multiValueMode);
    return builder;
  }

  @Override
  protected void innerWriteTo(StreamOutput out) {
    // Do nothing, no extra state to write to stream
  }

  @Override
  protected int innerHashCode() {
    return 0;
  }

  @Override
  protected boolean innerEquals(Object obj) {
    return true;
  }
}
