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
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.fielddata.IndexFieldData;
import org.elasticsearch.index.fielddata.IndexGeoPointFieldData;
import org.elasticsearch.index.fielddata.IndexNumericFieldData;
import org.elasticsearch.index.mapper.MappedFieldType;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.DocValueFormat;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationInitializationException;
import org.elasticsearch.search.aggregations.AggregatorFactories;
import org.elasticsearch.search.aggregations.AggregatorFactories.Builder;
import org.elasticsearch.search.aggregations.AggregatorFactory;
import org.elasticsearch.search.internal.SearchContext;

public abstract class MultiValuesSourceAggregationBuilder<VS extends ValuesSource, AB extends MultiValuesSourceAggregationBuilder<VS, AB>>
    extends AbstractAggregationBuilder<AB> {

  private static final Logger LOGGER = Loggers.getLogger(MultiValuesSourceAggregationBuilder.class);
  public static final ParseField MULTIVALUE_MODE_FIELD = new ParseField("mode");
  private final ValuesSourceType valuesSourceType;
  private final ValueType targetValueType;
  private final Object missing = null;
  private List<String> fields = Collections.emptyList();
  private ValueType valueType = null;
  private String format = null;
  private Map<String, Object> missingMap = Collections.emptyMap();

  protected MultiValuesSourceAggregationBuilder(final String name,
      final ValuesSourceType valuesSourceType, final ValueType targetValueType) {
    super(name);
    if (valuesSourceType == null) {
      throw new IllegalArgumentException("[valuesSourceType] must not be null: [" + name + "]");
    }
    this.valuesSourceType = valuesSourceType;
    this.targetValueType = targetValueType;
  }

  protected MultiValuesSourceAggregationBuilder(final StreamInput in,
      final ValuesSourceType valuesSourceType, final ValueType targetValueType)
      throws IOException {
    super(in);
    assert false
        == serializeTargetValueType() : "Wrong read constructor called for subclass that provides its targetValueType";
    this.valuesSourceType = valuesSourceType;
    this.targetValueType = targetValueType;
    read(in);
  }

  protected MultiValuesSourceAggregationBuilder(final StreamInput in,
      final ValuesSourceType valuesSourceType) throws IOException {
    super(in);
    assert serializeTargetValueType() : "Wrong read constructor called for subclass that serializes its targetValueType";
    this.valuesSourceType = valuesSourceType;
    this.targetValueType = in.readOptionalWriteable(ValueType::readFromStream);
    read(in);
  }

  private static DocValueFormat resolveFormat(@Nullable final String format,
      @Nullable final ValueType valueType) {
    if (valueType == null) {
      return DocValueFormat.RAW; // we can't figure it out
    }
    DocValueFormat valueFormat = valueType.defaultFormat();
    if (valueFormat instanceof DocValueFormat.Decimal && format != null) {
      valueFormat = new DocValueFormat.Decimal(format);
    }
    return valueFormat;
  }

  /**
   * Read from a stream.
   */
  @SuppressWarnings("unchecked")
  private void read(final StreamInput in) throws IOException {
    this.fields = (ArrayList<String>) in.readGenericValue();
    this.valueType = in.readOptionalWriteable(ValueType::readFromStream);
    this.format = in.readOptionalString();
    this.missingMap = in.readMap();
  }

  @Override
  protected final void doWriteTo(final StreamOutput out) throws IOException {
    if (serializeTargetValueType()) {
      out.writeOptionalWriteable(this.targetValueType);
    }
    out.writeGenericValue(this.fields);
    out.writeOptionalWriteable(this.valueType);
    out.writeOptionalString(this.format);
    out.writeMap(this.missingMap);
    innerWriteTo(out);
  }

  /**
   * Write subclass' state to the stream
   */
  protected abstract void innerWriteTo(StreamOutput out) throws IOException;

  /**
   * Sets the field to use for this aggregation.
   */
  @SuppressWarnings("unchecked")
  public AB fields(final List<String> fields) {
    if (fields == null) {
      throw new IllegalArgumentException("[field] must not be null: [" + this.name + "]");
    }
    this.fields = new ArrayList<>(fields);
    return (AB) this;
  }

  /**
   * Gets the field to use for this aggregation.
   */
  public List<String> fields() {
    return this.fields;
  }

  /**
   * Sets the {@link ValueType} for the value produced by this aggregation
   */
  @SuppressWarnings("unchecked")
  public AB valueType(final ValueType valueType) {
    if (valueType == null) {
      throw new IllegalArgumentException("[valueType] must not be null: [" + this.name + "]");
    }
    this.valueType = valueType;
    return (AB) this;
  }

  /**
   * Gets the {@link ValueType} for the value produced by this aggregation
   */
  public ValueType valueType() {
    return this.valueType;
  }

  /**
   * Sets the format to use for the output of the aggregation.
   */
  @SuppressWarnings("unchecked")
  public AB format(final String format) {
    if (format == null) {
      throw new IllegalArgumentException("[format] must not be null: [" + this.name + "]");
    }
    this.format = format;
    return (AB) this;
  }

  /**
   * Gets the format to use for the output of the aggregation.
   */
  public String format() {
    return this.format;
  }

  /**
   * Sets the value to use when the aggregation finds a missing value in a
   * document
   */
  @SuppressWarnings("unchecked")
  public AB missingMap(final Map<String, Object> missingMap) {
    if (missingMap == null) {
      throw new IllegalArgumentException("[missing] must not be null: [" + this.name + "]");
    }
    this.missingMap = missingMap;
    return (AB) this;
  }

  /**
   * Gets the value to use when the aggregation finds a missing value in a
   * document
   */
  public Map<String, Object> missingMap() {
    return this.missingMap;
  }

  @Override
  protected final MultiValuesSourceAggregatorFactory<VS, ?> doBuild(final SearchContext context,
      final AggregatorFactory<?> parent,
      final AggregatorFactories.Builder subFactoriesBuilder) throws IOException {
    final Map<String, ValuesSourceConfig<VS>> configs = resolveConfig(context);
    final MultiValuesSourceAggregatorFactory<VS, ?> factory = innerBuild(context, configs, parent,
        subFactoriesBuilder);
    return factory;
  }

  protected Map<String, ValuesSourceConfig<VS>> resolveConfig(final SearchContext context) {
    final Map<String, ValuesSourceConfig<VS>> configs = new LinkedHashMap<>();
    for (final String field : this.fields) {
      final ValuesSourceConfig<VS> config = config(context, field, null);
      LOGGER.debug("Resolved config for field {}: {}", field, config);
      configs.put(field, config);
    }
    return configs;
  }

  protected abstract MultiValuesSourceAggregatorFactory<VS, ?> innerBuild(SearchContext context,
      Map<String, ValuesSourceConfig<VS>> configs, AggregatorFactory<?> parent,
      AggregatorFactories.Builder subFactoriesBuilder) throws IOException;

  public ValuesSourceConfig<VS> config(final SearchContext context, final String field,
      final Script script) {

    final ValueType valueType = this.valueType != null ? this.valueType : this.targetValueType;

    if (field == null) {
      if (script == null) {
        final ValuesSourceConfig<VS> config = new ValuesSourceConfig<>(ValuesSourceType.ANY);
        return config.format(resolveFormat(null, valueType));
      }
      ValuesSourceType valuesSourceType =
          valueType != null ? valueType.getValuesSourceType() : this.valuesSourceType;
      if (valuesSourceType == null || valuesSourceType == ValuesSourceType.ANY) {
        // the specific value source type is undefined, but for scripts,
        // we need to have a specific value source
        // type to know how to handle the script values, so we fallback
        // on Bytes
        valuesSourceType = ValuesSourceType.BYTES;
      }
      final ValuesSourceConfig<VS> config = new ValuesSourceConfig<>(valuesSourceType);
      config.missing(this.missingMap.get(field));
      return config.format(resolveFormat(this.format, valueType));
    }

    final MappedFieldType fieldType = context.smartNameFieldType(field);
    if (fieldType == null) {
      final ValuesSourceType valuesSourceType =
          valueType != null ? valueType.getValuesSourceType() : this.valuesSourceType;
      final ValuesSourceConfig<VS> config = new ValuesSourceConfig<>(valuesSourceType);
      config.missing(this.missingMap.get(field));
      config.format(resolveFormat(this.format, valueType));
      return config.unmapped(true);
    }

    final IndexFieldData<?> indexFieldData = context.getForField(fieldType);

    final ValuesSourceConfig<VS> config;
    if (this.valuesSourceType == ValuesSourceType.ANY) {
      if (indexFieldData instanceof IndexNumericFieldData) {
        config = new ValuesSourceConfig<>(ValuesSourceType.NUMERIC);
      } else if (indexFieldData instanceof IndexGeoPointFieldData) {
        config = new ValuesSourceConfig<>(ValuesSourceType.GEOPOINT);
      } else {
        config = new ValuesSourceConfig<>(ValuesSourceType.BYTES);
      }
    } else {
      config = new ValuesSourceConfig<>(this.valuesSourceType);
    }

    config.fieldContext(new FieldContext(field, indexFieldData, fieldType));
    config.missing(this.missingMap.get(field));
    return config.format(fieldType.docValueFormat(this.format, null));
  }

  /**
   * Should this builder serialize its targetValueType? Defaults to false. All subclasses that
   * override this to true should use the three argument read constructor rather than the four
   * argument version.
   */
  protected boolean serializeTargetValueType() {
    return false;
  }

  @Override
  public final XContentBuilder internalXContent(final XContentBuilder builder, final Params params)
      throws IOException {
    builder.startObject();
    // todo add ParseField support to XContentBuilder
    if (this.fields != null) {
      builder.field(CommonFields.FIELDS.getPreferredName(), this.fields);
    }
    if (this.missing != null) {
      builder.field(CommonFields.MISSING.getPreferredName(), this.missing);
    }
    if (this.format != null) {
      builder.field(CommonFields.FORMAT.getPreferredName(), this.format);
    }
    if (this.valueType != null) {
      builder.field(CommonFields.VALUE_TYPE.getPreferredName(), this.valueType.getPreferredName());
    }
    doXContentBody(builder, params);
    builder.endObject();
    return builder;
  }

  protected abstract XContentBuilder doXContentBody(XContentBuilder builder, Params params)
      throws IOException;

  @Override
  protected final int doHashCode() {
    return Objects.hash(this.fields, this.format, this.missing, this.targetValueType,
        this.valueType,
        this.valuesSourceType,
        innerHashCode());
  }

  protected abstract int innerHashCode();

  @Override
  protected final boolean doEquals(final Object obj) {
    final MultiValuesSourceAggregationBuilder<?, ?> other = (MultiValuesSourceAggregationBuilder<?, ?>) obj;
    if (!Objects.equals(this.fields, other.fields)) {
      return false;
    }
    if (!Objects.equals(this.format, other.format)) {
      return false;
    }
    if (!Objects.equals(this.missing, other.missing)) {
      return false;
    }
    if (!Objects.equals(this.targetValueType, other.targetValueType)) {
      return false;
    }
    if (!Objects.equals(this.valueType, other.valueType)) {
      return false;
    }
    if (!Objects.equals(this.valuesSourceType, other.valuesSourceType)) {
      return false;
    }
    return innerEquals(obj);
  }

  protected abstract boolean innerEquals(Object obj);

  public abstract static class LeafOnly<VS extends ValuesSource, AB extends MultiValuesSourceAggregationBuilder<VS, AB>>
      extends MultiValuesSourceAggregationBuilder<VS, AB> {

    protected LeafOnly(final String name, final ValuesSourceType valuesSourceType,
        final ValueType targetValueType) {
      super(name, valuesSourceType, targetValueType);
    }

    /**
     * Read from a stream that does not serialize its targetValueType. This should be used by most
     * subclasses.
     */
    protected LeafOnly(
        final StreamInput in, final ValuesSourceType valuesSourceType,
        final ValueType targetValueType) throws IOException {
      super(in, valuesSourceType, targetValueType);
    }

    /**
     * Read an aggregation from a stream that serializes its targetValueType. This should only be
     * used by subclasses that override {@link #serializeTargetValueType()} to return true.
     */
    protected LeafOnly(final StreamInput in, final ValuesSourceType valuesSourceType)
        throws IOException {
      super(in, valuesSourceType);
    }

    @Override
    public AB subAggregations(final Builder subFactories) {
      throw new AggregationInitializationException("Aggregator [" + this.name + "] of type [" +
          getType() + "] cannot accept sub-aggregations");
    }
  }
}