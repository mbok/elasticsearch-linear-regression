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

import static org.elasticsearch.search.aggregations.support.MultiValuesSourceAggregationBuilder.MULTIVALUE_MODE_FIELD;

import java.io.IOException;
import java.util.Map;
import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.search.MultiValueMode;
import org.elasticsearch.search.aggregations.support.MultiValuesSourceParser.NumericValuesSourceParser;
import org.elasticsearch.search.aggregations.support.ValueType;
import org.elasticsearch.search.aggregations.support.ValuesSourceType;

/**
 * Created by mbok on 02.04.17.
 */
public abstract class BaseParser<B extends BaseAggregationBuilder<B>> extends
    NumericValuesSourceParser {

  protected BaseParser() {
    super(true);
  }

  @Override
  protected boolean token(final String aggregationName, final String currentFieldName,
      final XContentParser.Token token, final XContentParser parser,
      final Map<ParseField, Object> otherOptions) throws IOException {
    if (MULTIVALUE_MODE_FIELD.match(currentFieldName)
        && token == XContentParser.Token.VALUE_STRING) {
      otherOptions.put(MULTIVALUE_MODE_FIELD, parser.text());
      return true;
    }
    return false;
  }

  @Override
  protected B createFactory(final String aggregationName,
      final ValuesSourceType valuesSourceType,
      final ValueType targetValueType, final Map<ParseField, Object> otherOptions) {
    final B builder = createInnerFactory(aggregationName, otherOptions);
    final String mode = (String) otherOptions.get(MULTIVALUE_MODE_FIELD);
    if (mode != null) {
      builder.multiValueMode(MultiValueMode.fromString(mode));
    }
    return builder;
  }

  protected abstract B createInnerFactory(String aggregationName,
      Map<ParseField, Object> otherOptions);
}
