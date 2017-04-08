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
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.io.stream.Writeable;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.scaleborn.linereg.evaluation.SlopeCoefficients;
import org.scaleborn.linereg.evaluation.SlopeCoefficients.DefaultSlopeCoefficients;

/**
 * Created by mbok on 07.04.17.
 */
public class ModelResults implements Writeable, ToXContent {

  private SlopeCoefficients slopeCoefficients;

  private Double intercept;

  public ModelResults(final SlopeCoefficients slopeCoefficients) {
    this.slopeCoefficients = slopeCoefficients;
  }

  public ModelResults(final StreamInput in) throws IOException {
    this.slopeCoefficients = new DefaultSlopeCoefficients(in.readDoubleArray());
    this.intercept = in.readOptionalDouble();
  }

  @Override
  public void writeTo(final StreamOutput out) throws IOException {
    out.writeDoubleArray(this.slopeCoefficients.getCoefficients());
    out.writeOptionalDouble(this.intercept);
  }

  public SlopeCoefficients getSlopeCoefficients() {
    return this.slopeCoefficients;
  }

  public void setSlopeCoefficients(final SlopeCoefficients slopeCoefficients) {
    this.slopeCoefficients = slopeCoefficients;
  }

  public double getIntercept() {
    return this.intercept != null ? this.intercept : 0;
  }

  public void setIntercept(final double intercept) {
    this.intercept = intercept;
  }


  @Override
  public String toString() {
    return "ModelResults{" +
        "slopeCoefficients=" + this.slopeCoefficients +
        ", intercept=" + this.intercept +
        '}';
  }

  @Override
  public XContentBuilder toXContent(final XContentBuilder builder, final Params params)
      throws IOException {
    builder.array("coefficients", this.getSlopeCoefficients().getCoefficients());
    if (this.intercept != null) {
      builder.field("intercept", this.getIntercept());
    }
    return builder;
  }

}
