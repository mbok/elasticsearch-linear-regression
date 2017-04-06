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
    featureSums = new double[featuresCount];
    featuresResponseProductSum = new double[featuresCount];
  }

  @Override
  public void sample(double[] featureValues, double targetValue) {
    super.sample(featureValues, targetValue);
    for (int i = 0; i < featuresCount; i++) {
      double v = featureValues[i];
      featureSums[i] += v;
      featuresResponseProductSum[i] += v * targetValue;
    }
    responseSum += targetValue;
    responseSquareSum += targetValue * targetValue;
  }

  @Override
  public void merge(ExactSamplingContext from) {
    super.merge(from);
    for (int i = 0; i < featuresCount; i++) {
      featureSums[i] += from.featureSums[i];
      featuresResponseProductSum[i] += from.featuresResponseProductSum[i];
    }
    responseSum += from.responseSum;
    responseSquareSum += from.responseSquareSum;
  }

  double[] getFeaturesMean() {
    double[] avgs = new double[featuresCount];
    if (count > 0) {
      for (int i = 0; i < featuresCount; i++) {
        avgs[i] = featureSums[i] / count;
      }
    }
    return avgs;
  }

  double getResponseMean() {
    if (count > 0) {
      return responseSum / count;
    }
    return 0;
  }

  @Override
  public void saveState(final StateOutputStream stream) throws IOException {
    stream.writeDouble(responseSum);
    stream.writeDouble(responseSquareSum);
    stream.writeDoubleArray(featureSums);
    stream.writeDoubleArray(featuresResponseProductSum);
  }

  @Override
  public void loadState(final StateInputStream stream) throws IOException {
    responseSum = stream.readDouble();
    responseSquareSum = stream.readDouble();
    featureSums = stream.readDoubleArray();
    featuresResponseProductSum = stream.readDoubleArray();
  }
}
