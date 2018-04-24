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
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.Explanation;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.io.stream.Writeable;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.common.lucene.search.function.LeafScoreFunction;
import org.elasticsearch.common.lucene.search.function.ScoreFunction;
import org.elasticsearch.index.fielddata.IndexNumericFieldData;
import org.elasticsearch.index.fielddata.SortedNumericDoubleValues;

/**
 * Created by mbok on 12.09.17.
 */
public class LinearRegressionErrorScoreFunction extends ScoreFunction {

  private final String[] fields;
  private final IndexNumericFieldData[] fieldDatas;
  private final double[] coefficients;
  private final Modifier modifier;
  private final Double[] missing;

  public LinearRegressionErrorScoreFunction(final String[] fields,
      final IndexNumericFieldData[] fieldDatas, final double[] coefficients,
      final Modifier modifier,
      final Double[] missing) {
    super(CombineFunction.MULTIPLY);
    this.fields = fields;
    this.fieldDatas = fieldDatas;
    this.coefficients = coefficients;
    this.modifier = modifier;
    this.missing = missing;
  }

  private double error(final double[] values) {
    final int count = this.fields.length;
    double error = values[count - 1] - this.coefficients[0];
    for (int i = 1; i < count; i++) {
      error -= this.coefficients[i] * values[i - 1];
    }
    return error;
  }

  @Override
  public LeafScoreFunction getLeafScoreFunction(final LeafReaderContext ctx) {
    final int fieldsCount = this.fields.length;
    final SortedNumericDoubleValues[] values = new SortedNumericDoubleValues[fieldsCount];
    for (int i = 0; i < fieldsCount; ++i) {
      if (this.fieldDatas[i] != null) {
        values[i] = this.fieldDatas[i].load(ctx).getDoubleValues();
      }
    }
    return new LeafScoreFunction() {
      final double[] fieldVals = new double[fieldsCount];

      @Override
      public double score(final int docId, final float subQueryScore) throws IOException {
        // get fields
        loadFieldVals(docId);
        // Calculate error
        final double error = error(this.fieldVals);
        final double result = LinearRegressionErrorScoreFunction.this.modifier.apply(error);
        if (Double.isNaN(result) || Double.isInfinite(result)) {
          throw new ElasticsearchException(
              "Result of field modification [" + LinearRegressionErrorScoreFunction.this.modifier
                  .toString() + "(" + error
                  + ")] must be a number");
        }
        return result;
      }

      @Override
      public Explanation explainScore(final int docId, final Explanation subQueryScore)
          throws IOException {
        final String modifierStr =
            LinearRegressionErrorScoreFunction.this.modifier
                != null ? LinearRegressionErrorScoreFunction.this.modifier.toString() : "";
        final double score = score(docId, subQueryScore.getValue());
        return Explanation.match(
            (float) score,
            String.format(Locale.ROOT,
                "linear regression error function: %s(fields=%s, coefficients=%s, missing=%s)",
                modifierStr,
                Arrays.toString(LinearRegressionErrorScoreFunction.this.fields),
                Arrays.toString(LinearRegressionErrorScoreFunction.this.coefficients),
                Arrays.toString(LinearRegressionErrorScoreFunction.this.missing)));
      }

      private void loadFieldVals(final int docId) throws IOException {
        // loop over fields
        for (int i = 0; i < fieldsCount; ++i) {
          final SortedNumericDoubleValues doubleValues = values[i];
          double value = 0;
          if (doubleValues != null) {
            if (doubleValues.advanceExact(docId)) {
              value = doubleValues.nextValue();
            } else {
              if (LinearRegressionErrorScoreFunction.this.missing[i] != null) {
                value = LinearRegressionErrorScoreFunction.this.missing[i];
              } else {
                throw new ElasticsearchException(
                    "Missing value for field [" + LinearRegressionErrorScoreFunction.this.fields[i]
                        + "] in document [" + docId + "]");
              }
            }
          }
          this.fieldVals[i] = value;
        }
      }
    };
  }

  @Override
  public boolean needsScores() {
    return false;
  }

  @Override
  protected boolean doEquals(final ScoreFunction o) {
    final LinearRegressionErrorScoreFunction other = (LinearRegressionErrorScoreFunction) o;
    return Arrays.equals(this.fields, other.fields) &&
        Arrays.equals(this.coefficients, other.coefficients) &&
        Objects.equals(this.modifier, other.modifier) && Arrays
        .equals((Object[]) this.missing, other.missing);
  }

  @Override
  protected int doHashCode() {
    return Objects.hash(this.fields, this.coefficients, this.modifier, this.missing);
  }

  /**
   * The Type class encapsulates the modification types that can be applied
   * to the error.
   */
  public enum Modifier implements Writeable {
    NONE {
      @Override
      public double apply(final double n) {
        return n;
      }
    }, ABS {
      @Override
      public double apply(final double n) {
        return Math.abs(n);
      }
    }, SQUARE {
      @Override
      public double apply(final double n) {
        return n * n;
      }
    },
    RECIPROCAL {
      @Override
      public double apply(final double n) {
        return n != 0 ? 1.0 / n : Double.MAX_VALUE;
      }
    },
    ABS_RECIPROCAL {
      @Override
      public double apply(final double n) {
        return RECIPROCAL.apply(Math.abs(n));
      }
    },
    SQUARE_RECIPROCAL {
      @Override
      public double apply(final double n) {
        return RECIPROCAL.apply(n * n);
      }
    };

    public static Modifier readFromStream(final StreamInput in)
        throws IOException {
      return in.readEnum(Modifier.class);
    }

    public static Modifier fromString(final String modifier) {
      return valueOf(modifier.toUpperCase(Locale.ROOT));
    }

    public abstract double apply(double n);

    @Override
    public void writeTo(final StreamOutput out) throws IOException {
      out.writeEnum(this);
    }

    @Override
    public String toString() {
      return super.toString().toLowerCase(Locale.ROOT);
    }
  }
}
