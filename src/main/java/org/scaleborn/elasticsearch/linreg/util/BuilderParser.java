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

package org.scaleborn.elasticsearch.linreg.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.elasticsearch.common.ParsingException;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentParser.Token;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.aggregations.AggregationBuilder.CommonFields;
import org.scaleborn.elasticsearch.linreg.util.BuilderParser.BaseBuilder;

/**
 * Created by mbok on 18.09.17.
 */
public abstract class BuilderParser<B extends BaseBuilder> {

  public interface BaseBuilder {

    BaseBuilder fields(String[] fields);

    BaseBuilder missing(Map<String, Double> missing);
  }

  public final void parse(final B builder, final String builderName,
      final XContentParser parser)
      throws IOException {
    List<String> fields = null;
    Map<String, Double> missingMap = null;
    Token token;
    String currentFieldName = null;
    while ((token = parser.nextToken()) != Token.END_OBJECT) {
      if (token == Token.FIELD_NAME) {
        currentFieldName = parser.currentName();
      } else if (token == Token.VALUE_STRING) {
        if (CommonFields.FIELDS.match(currentFieldName)) {
          fields = Collections.singletonList(parser.text());
        } else if (!token(builder, builderName, currentFieldName, token, parser)) {
          throw new ParsingException(parser.getTokenLocation(),
              "Unexpected token " + token + " [" + currentFieldName + "] in [" + builderName
                  + "].");
        }
      } else if (token == Token.START_OBJECT) {
        if (CommonFields.MISSING.match(currentFieldName)) {
          missingMap = new HashMap<>();
          while (parser.nextToken() != Token.END_OBJECT) {
            parseMissingAndAdd(builderName, currentFieldName, parser, missingMap);
          }
        } else if (!token(builder, builderName, currentFieldName, token, parser)) {
          throw new ParsingException(parser.getTokenLocation(),
              "Unexpected token " + token + " [" + currentFieldName + "] in [" + builderName
                  + "].");
        }
      } else if (token == Token.START_ARRAY) {
        if (Script.SCRIPT_PARSE_FIELD.match(currentFieldName)) {
          throw new ParsingException(parser.getTokenLocation(),
              "Unexpected token " + token + " [" + currentFieldName + "] in [" + builderName
                  + "]. " +
                  "Multi-field aggregations do not support scripts.");
        } else if (CommonFields.FIELDS.match(currentFieldName)) {
          fields = new ArrayList<>();
          while ((token = parser.nextToken()) != Token.END_ARRAY) {
            if (token == Token.VALUE_STRING) {
              fields.add(parser.text());
            } else {
              throw new ParsingException(parser.getTokenLocation(),
                  "Unexpected token " + token + " [" + currentFieldName + "] in [" + builderName
                      + "].");
            }
          }
        } else if (!token(builder, builderName, currentFieldName, token, parser)) {
          throw new ParsingException(parser.getTokenLocation(),
              "Unexpected token " + token + " [" + currentFieldName + "] in [" + builderName
                  + "].");
        }
      } else if (!token(builder, builderName, currentFieldName, token, parser)) {
        throw new ParsingException(parser.getTokenLocation(),
            "Unexpected token " + token + " [" + currentFieldName + "] in [" + builderName
                + "].");
      }
    }
    builder.fields(fields != null ? fields.toArray(new String[0]) : new String[0]);
    builder.missing(missingMap);
  }

  private void parseMissingAndAdd(final String aggregationName, final String currentFieldName,
      final XContentParser parser, final Map<String, Double> missing) throws IOException {
    XContentParser.Token token = parser.currentToken();
    if (token == null) {
      token = parser.nextToken();
    }

    if (token == XContentParser.Token.FIELD_NAME) {
      final String fieldName = parser.currentName();
      if (missing.containsKey(fieldName)) {
        throw new ParsingException(parser.getTokenLocation(),
            "Missing field [" + fieldName + "] already defined as [" + missing.get(fieldName)
                + "] in [" + aggregationName + "].");
      }
      parser.nextToken();
      missing.put(fieldName, parser.doubleValue());
    } else {
      throw new ParsingException(parser.getTokenLocation(),
          "Unexpected token " + token + " [" + currentFieldName + "] in [" + aggregationName + "]");
    }
  }

  /**
   * Allows subclasses of {@link BuilderParser} to parse extra
   * parameters.
   *
   * @param builder the builder
   * @param builderName the name of the aggregation
   * @param currentFieldName the name of the current field being parsed
   * @param token the current token for the parser
   * @param parser the parser
   * @return <code>true</code> if the current token was correctly parsed, <code>false</code>
   * otherwise
   * @throws IOException if an error occurs whilst parsing
   */
  protected abstract boolean token(B builder, String builderName, String currentFieldName,
      XContentParser.Token token, XContentParser parser) throws IOException;
}
