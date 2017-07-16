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
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.search.aggregations.InternalAggregation.CommonFields;
import org.scaleborn.elasticsearch.linreg.aggregation.support.ModelResults;
import org.scaleborn.linereg.estimation.SlopeCoefficients;

/**
 * Created by mbok on 11.04.17.
 */
public class PredictionResults extends ModelResults {

  private final double predictedValue;

  public PredictionResults(final double predictedValue,
      final SlopeCoefficients slopeCoefficients, final double intercept) {
    super(slopeCoefficients, intercept);
    this.predictedValue = predictedValue;
  }

  public PredictionResults(final StreamInput in)
      throws IOException {
    super(in);
    this.predictedValue = in.readDouble();
  }

  @Override
  public void writeTo(final StreamOutput out) throws IOException {
    super.writeTo(out);
    out.writeDouble(this.predictedValue);
  }

  @Override
  public XContentBuilder toXContent(final XContentBuilder builder, final Params params)
      throws IOException {
    builder.field(CommonFields.VALUE, this.predictedValue);
    return super.toXContent(builder, params);
  }

  public double getPredictedValue() {
    return this.predictedValue;
  }
}
