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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.AggregatorFactories;
import org.elasticsearch.search.aggregations.AggregatorFactory;
import org.elasticsearch.search.aggregations.InternalAggregation.Type;
import org.elasticsearch.search.aggregations.support.ValueType;
import org.elasticsearch.search.aggregations.support.ValuesSource.Numeric;
import org.elasticsearch.search.aggregations.support.ValuesSourceConfig;
import org.elasticsearch.search.internal.SearchContext;

/**
 * Created by mbok on 22.03.17.
 */
public abstract class BaseAggregationBuilder<AB extends BaseAggregationBuilder<AB>> extends
    AbstractAggregationBuilder<AB> {

  private List<String> featureFields;
  private String responseField;

  public BaseAggregationBuilder(final String name,
      final Type type) {
    super(name, type);
  }

  protected BaseAggregationBuilder(final StreamInput in,
      final Type type) throws IOException {
    super(in, type);
    read(in);
  }

  /**
   * Read from a stream.
   */
  private void read(StreamInput in) throws IOException {
    featureFields = Arrays.asList(in.readStringArray());
    responseField = in.readString();
  }

  @Override
  protected void doWriteTo(final StreamOutput out) throws IOException {
    out.writeStringArray(featureFields.toArray(new String[featureFields.size()]));
    out.writeString(responseField);
    innerWriteTo(out);
  }

  /**
   * Write subclass's state to the stream if required.
   *
   * @param out the output stream
   */
  protected void innerWriteTo(StreamOutput out) throws IOException {
    // NOP
  }


  @Override
  protected final BaseAggregatorFactory<?> doBuild(SearchContext context,
      AggregatorFactory<?> parent,
      AggregatorFactories.Builder subFactoriesBuilder) throws IOException {
    List<ValuesSourceConfig<Numeric>> featureConfigs = new ArrayList<>(featureFields.size());
    for (String featureField : featureFields) {
      featureConfigs.add(ValuesSourceConfig.resolve(context.getQueryShardContext(),
          ValueType.NUMERIC, featureField, null, null, null, null));
    }
    ValuesSourceConfig<Numeric> responseConfig = ValuesSourceConfig
        .resolve(context.getQueryShardContext(),
            ValueType.NUMERIC, responseField, null, null, null, null);
    BaseAggregatorFactory<?> factory = innerBuild(context,
        featureConfigs,
        responseConfig, parent,
        subFactoriesBuilder);
    return factory;
  }


  protected abstract BaseAggregatorFactory<?> innerBuild(SearchContext context,
      List<ValuesSourceConfig<Numeric>> featureConfigs, ValuesSourceConfig<Numeric> responseConfig,
      AggregatorFactory<?> parent, AggregatorFactories.Builder subFactoriesBuilder)
      throws IOException;


  @Override
  public final XContentBuilder internalXContent(XContentBuilder builder, Params params)
      throws IOException {
    builder.startObject();
    if (featureFields != null) {
      builder.field("feature_fields", featureFields);
    }
    if (responseField != null) {
      builder.field("response_field", responseField);
    }
    doXContentBody(builder, params);
    builder.endObject();
    return builder;
  }

  /**
   * Override in sub classes if required.
   */
  protected XContentBuilder doXContentBody(XContentBuilder builder, Params params)
      throws IOException {
    return builder;
  }

  @Override
  protected final int doHashCode() {
    return Objects.hash(featureFields, responseField, innerHashCode());
  }

  /**
   * Override in sub classes to include further attributes.
   */
  protected int innerHashCode() {
    return 0;
  }

  @Override
  protected boolean doEquals(final Object obj) {
    BaseAggregationBuilder other = (BaseAggregationBuilder) obj;
    if (!Objects.equals(featureFields, other.featureFields)) {
      return false;
    }
    if (!Objects.equals(responseField, other.responseField)) {
      return false;
    }
    return innerEquals(obj);
  }

  /**
   * Override in sub classes to include further attributes.
   */
  protected boolean innerEquals(Object obj) {
    return true;
  }

}
