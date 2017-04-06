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

package org.scaleborn.linereg.sampling.exact;

import java.io.IOException;
import java.util.Arrays;
import org.scaleborn.linereg.sampling.io.StateInputStream;
import org.scaleborn.linereg.sampling.io.StateOutputStream;
import org.scaleborn.linereg.sampling.support.BaseSamplingContext;

/**
 * Created by mbok on 26.03.17.
 */
public class ExactSamplingContext extends BaseSamplingContext<ExactSamplingContext> {

  protected double[] featureSums;
  protected double responseSum = 0;

  // TODO: Big values possible, switch to online
  protected double[] featuresResponseProductSum;
  protected double responseSquareSum = 0;

  public ExactSamplingContext(final int featuresCount) {
    super(featuresCount);
    this.featureSums = new double[featuresCount];
    this.featuresResponseProductSum = new double[featuresCount];
  }

  @Override
  public void sample(double[] featureValues, double targetValue) {
    super.sample(featureValues, targetValue);
    for (int i = 0; i < featuresCount; i++) {
      double v = featureValues[i];
      this.featureSums[i] += v;
      this.featuresResponseProductSum[i] += v * targetValue;
    }
    this.responseSum += targetValue;
    this.responseSquareSum += targetValue * targetValue;
  }

  @Override
  public void merge(ExactSamplingContext from) {
    super.merge(from);
    for (int i = 0; i < featuresCount; i++) {
      this.featureSums[i] += from.featureSums[i];
      this.featuresResponseProductSum[i] += from.featuresResponseProductSum[i];
    }
    this.responseSum += from.responseSum;
    this.responseSquareSum += from.responseSquareSum;
  }

  double[] getFeaturesMean() {
    double[] avgs = new double[featuresCount];
    if (count > 0) {
      for (int i = 0; i < featuresCount; i++) {
        avgs[i] = this.featureSums[i] / count;
      }
    }
    return avgs;
  }

  double getResponseMean() {
    if (count > 0) {
      return this.responseSum / count;
    }
    return 0;
  }

  @Override
  public void saveState(final StateOutputStream stream) throws IOException {
    super.saveState(stream);
    stream.writeDouble(this.responseSum);
    stream.writeDouble(this.responseSquareSum);
    stream.writeDoubleArray(this.featureSums);
    stream.writeDoubleArray(this.featuresResponseProductSum);
  }

  @Override
  public void loadState(final StateInputStream stream) throws IOException {
    super.loadState(stream);
    this.responseSum = stream.readDouble();
    this.responseSquareSum = stream.readDouble();
    this.featureSums = stream.readDoubleArray();
    this.featuresResponseProductSum = stream.readDoubleArray();
  }

  @Override
  public String toString() {
    return "ExactSamplingContext{" +
        "featureSums=" + Arrays.toString(this.featureSums) +
        ", responseSum=" + this.responseSum +
        ", featuresResponseProductSum=" + Arrays.toString(this.featuresResponseProductSum) +
        ", responseSquareSum=" + this.responseSquareSum +
        "} " + super.toString();
  }
}
