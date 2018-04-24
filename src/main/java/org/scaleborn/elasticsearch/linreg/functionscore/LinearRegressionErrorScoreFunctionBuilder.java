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

package org.scaleborn.elasticsearch.linreg.functionscore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.common.ParsingException;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.lucene.search.function.ScoreFunction;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentParser.Token;
import org.elasticsearch.index.fielddata.IndexNumericFieldData;
import org.elasticsearch.index.mapper.MappedFieldType;
import org.elasticsearch.index.query.QueryShardContext;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilder;
import org.scaleborn.elasticsearch.linreg.functionscore.LinearRegressionErrorScoreFunction.Modifier;
import org.scaleborn.elasticsearch.linreg.util.BuilderParser;
import org.scaleborn.elasticsearch.linreg.util.BuilderParser.BaseBuilder;

/**
 * Created by mbok on 11.09.17.
 */
public class LinearRegressionErrorScoreFunctionBuilder extends
    ScoreFunctionBuilder<LinearRegressionErrorScoreFunctionBuilder> implements BaseBuilder {

  public static final String NAME = "linreg_error";
  private String[] fields;
  private double[] coefficients;
  private Modifier modifier = Modifier.ABS;
  private Map<String, Double> missing;

  public LinearRegressionErrorScoreFunctionBuilder() {
  }

  /**
   * Read from a stream.
   */
  public LinearRegressionErrorScoreFunctionBuilder(final StreamInput in) throws IOException {
    super(in);
    this.fields = in.readStringArray();
    this.coefficients = in.readDoubleArray();
    this.modifier = Modifier.readFromStream(in);
    if (in.readBoolean()) {
      this.missing = in.readMap(StreamInput::readString, StreamInput::readOptionalDouble);
    }
  }

  private static int hash(final long value) {
    return Long.hashCode(value);
  }

  public static LinearRegressionErrorScoreFunctionBuilder fromXContent(
      final XContentParser parser)
      throws IOException, ParsingException {
    final LinearRegressionErrorScoreFunctionBuilder builder = new LinearRegressionErrorScoreFunctionBuilder();
    final List<Double> coefficients = new ArrayList<>();
    new BuilderParser<LinearRegressionErrorScoreFunctionBuilder>() {

      @Override
      protected boolean token(final LinearRegressionErrorScoreFunctionBuilder builder,
          final String builderName,
          final String currentFieldName, Token token, final XContentParser parser)
          throws IOException {
        if (token.isValue()) {
          if ("modifier".equals(currentFieldName)) {
            if (token == XContentParser.Token.VALUE_STRING) {
              final String strModifier = parser.text();
              try {
                builder.modifier(Modifier.fromString(strModifier));
              } catch (final IllegalArgumentException e) {
                throw new ParsingException(parser.getTokenLocation(),
                    "Expected values '" + Arrays.toString(Modifier.values())
                        + "' in [linreg_error] for [modifier], not '"
                        + strModifier + "'", e);
              }
            } else {
              throw new ParsingException(parser.getTokenLocation(),
                  "String expected in [linreg_error] for [modifier], not '"
                      + token.toString() + "'");
            }
            return true;
          }
        } else if (token == Token.START_ARRAY && "coefficients".equals(currentFieldName)) {
          while ((token = parser.nextToken()) != Token.END_ARRAY) {
            if (token == Token.VALUE_NUMBER) {
              coefficients.add(parser.doubleValue());
            } else {
              throw new ParsingException(parser.getTokenLocation(),
                  "Unexpected token " + token + " [" + currentFieldName + "] in [" + builderName
                      + "].");
            }
          }
          return true;
        }
        return false;
      }
    }.parse(builder, "linreg_error", parser);
    final double[] cs = new double[coefficients.size()];
    int i = 0;
    for (final double c : coefficients) {
      cs[i++] = c;
    }
    builder.coefficients(cs);
    if (builder.coefficients.length < 2) {
      throw new IllegalArgumentException(
          "[coefficients] must have at least 2 values [" + builder.coefficients.length
              + "] in: ["
              + NAME
              + "]");
    }
    if (builder.coefficients.length != builder.fields.length) {
      throw new IllegalArgumentException(
          "[coefficients] must have [" + builder.fields.length
              + "] values as much as the number of fields (feature fields + response field). Given number of [coefficients] is ["
              + builder.coefficients.length + "] in: ["
              + NAME
              + "]");
    }
    return builder;
  }

  @Override
  protected void doWriteTo(final StreamOutput out) throws IOException {
    out.writeStringArray(this.fields);
    out.writeDoubleArray(this.coefficients);
    this.modifier.writeTo(out);
    out.writeBoolean(this.missing != null);
    if (this.missing != null) {
      out.writeMap(this.missing, StreamOutput::writeString, StreamOutput::writeOptionalDouble);
    }
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public LinearRegressionErrorScoreFunctionBuilder fields(final String[] fields) {
    this.fields = fields;
    return this;
  }

  public String[] fields() {
    return this.fields;
  }

  public LinearRegressionErrorScoreFunctionBuilder coefficients(final double[] coefficients) {
    this.coefficients = coefficients;
    return this;
  }

  public double[] coefficients() {
    return this.coefficients;
  }

  public Modifier modifier() {
    return this.modifier;
  }

  public LinearRegressionErrorScoreFunctionBuilder modifier(final Modifier modifier) {
    this.modifier = modifier;
    return this;
  }

  @Override
  public LinearRegressionErrorScoreFunctionBuilder missing(final Map<String, Double> missing) {
    this.missing = missing;
    return this;
  }

  public Map<String, Double> missing() {
    return this.missing;
  }

  @Override
  public void doXContent(final XContentBuilder builder, final Params params) throws IOException {
    builder.startObject(getName());
    builder.field("fields", this.fields);
    builder.field("coefficients", this.coefficients);
    builder.field("modifier", this.modifier.toString());
    if (this.missing != null) {
      builder.field("missing", this.missing);
    }
    builder.endObject();
  }

  @Override
  protected boolean doEquals(final LinearRegressionErrorScoreFunctionBuilder functionBuilder) {
    return Arrays.equals(this.fields, functionBuilder.fields) && Arrays
        .equals(this.coefficients, functionBuilder.coefficients) && Objects
        .equals(this.modifier, functionBuilder.modifier) && Objects
        .equals(this.missing, functionBuilder.missing);
  }

  @Override
  protected int doHashCode() {
    return Objects.hash(this.fields, this.coefficients, this.modifier, this.missing);
  }

  @Override
  protected ScoreFunction doToFunction(final QueryShardContext context) {
    final IndexNumericFieldData[] fieldDatas = new IndexNumericFieldData[this.fields.length];
    int i = 0;
    for (final String field : this.fields) {
      final MappedFieldType fieldType = context.getMapperService().fullName(field);
      IndexNumericFieldData fieldData = null;
      if (fieldType == null) {
        if (this.missing == null || !this.missing.containsKey(field)) {
          throw new ElasticsearchException(
              "Unable to find a field mapper for field [" + field
                  + "] nor a 'missing' value defined for the field.");
        }
      } else {
        fieldData = context.getForField(fieldType);
      }
      fieldDatas[i++] = fieldData;
    }
    final Double[] missingArray = new Double[this.fields.length];
    if (this.missing != null) {
      for (int j = 0; j < this.fields.length; j++) {
        missingArray[j] = this.missing.get(this.fields[j]);
      }
    }
    return new LinearRegressionErrorScoreFunction(this.fields, fieldDatas, this.coefficients,
        this.modifier,
        missingArray);
  }

}
