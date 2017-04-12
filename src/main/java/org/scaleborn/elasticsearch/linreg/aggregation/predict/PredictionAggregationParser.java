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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.ParsingException;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentParser.Token;
import org.scaleborn.elasticsearch.linreg.aggregation.support.BaseParser;

/**
 * Created by mbok on 11.04.17.
 */
public class PredictionAggregationParser extends BaseParser<PredictionAggregationBuilder> {

  private static final Logger LOGGER = Loggers.getLogger(PredictionAggregationParser.class);
  private static final ParseField INPUTS = new ParseField("inputs");


  @Override
  protected PredictionAggregationBuilder createInnerFactory(final String aggregationName,
      final Map<ParseField, Object> otherOptions) {
    final PredictionAggregationBuilder builder = new PredictionAggregationBuilder(aggregationName);
    if (otherOptions.containsKey(INPUTS)) {
      final List<Double> inputsList = (List<Double>) otherOptions.get(INPUTS);
      final double[] inputs = new double[inputsList.size()];
      int i = 0;
      for (final Double input : inputsList) {
        inputs[i++] = input;
      }
      builder.inputs(inputs);
    }
    return builder;
  }

  @Override
  protected boolean token(final String aggregationName, final String currentFieldName,
      Token token, final XContentParser parser, final Map<ParseField, Object> otherOptions)
      throws IOException {
    List<Double> inputFields = null;
    if (super.token(aggregationName, currentFieldName, token, parser, otherOptions)) {
      return true;
    } else if (INPUTS.match(currentFieldName)) {
      inputFields = new ArrayList<>();
      while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
        if (token == Token.VALUE_NUMBER) {
          inputFields.add(parser.numberValue().doubleValue());
        } else {
          throw new ParsingException(parser.getTokenLocation(),
              "Number value expected, but got token " + token + " [" + currentFieldName + "] in ["
                  + aggregationName
                  + "].");
        }
      }
      otherOptions.put(INPUTS, inputFields);
      LOGGER.info("Parsed input fields: {}", inputFields);
      return true;
    }
    return false;
  }
}
