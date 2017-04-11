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

package org.scaleborn.elasticsearch.linreg.aggregation.predict;

import java.util.Map;
import org.elasticsearch.common.ParseField;
import org.scaleborn.elasticsearch.linreg.aggregation.support.BaseParser;

/**
 * Created by mbok on 11.04.17.
 */
public class PredictionAggregationParser extends BaseParser<PredictionAggregationBuilder> {

  @Override
  protected PredictionAggregationBuilder createInnerFactory(final String aggregationName,
      final Map<ParseField, Object> otherOptions) {
    return new PredictionAggregationBuilder(aggregationName);
  }
}
