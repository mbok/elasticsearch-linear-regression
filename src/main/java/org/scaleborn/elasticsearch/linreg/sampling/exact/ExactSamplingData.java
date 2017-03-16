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

package org.scaleborn.elasticsearch.linreg.sampling.exact;

import org.scaleborn.elasticsearch.linreg.sampling.SamplingData;

/**
 * Sampling data for a linear model where the covariance matrix is solved exactly. Doing this that
 * way result in a time complexity of O(C² * N) and a memory consumption of O(C²), where C is
 * the count of variables and N the count of observations / documents.
 *
 * Created by mbok on 12.03.17.
 */
public class ExactSamplingData extends SamplingData {

  private double[][] featuresProductSums;

  public ExactSamplingData(int featuresCount) {
    super(featuresCount);
    featuresProductSums = new double[featuresCount][];
    for (int i = 0; i < featuresCount; i++) {
      featuresProductSums[i] = new double[featuresCount];
    }
  }

  @Override
  protected void doSample(double[] featureValues, double targetValue) {
    for (int i = 0; i < featuresCount; i++) {
      double vi = featureValues[i];
      for (int j = 0; j < featuresCount; j++) {
        featuresProductSums[i][j] += vi * featureValues[j];
      }
    }
  }

  @Override
  public void doMerge(SamplingData from) {
    for (int i = 0; i < featuresCount; i++) {
      for (int j = 0; j < featuresCount; j++) {
        featuresProductSums[i][j] += ((ExactSamplingData) from).featuresProductSums[i][j];
      }
    }
  }

  @Override
  public double[][] getCovarianceMatrix() {
    double[][] covMatrix = new double[featuresCount][];
    double[] avgs = getAverages();
    for (int i = 0; i < featuresCount; i++) {
      double avgI = avgs[i];
      covMatrix[i] = new double[featuresCount];
      for (int j = 0; j < featuresCount; j++) {
        double avgJ = avgs[j];
        covMatrix[i][j] = featuresProductSums[i][j] - avgI * featureSums[j] - avgJ * featureSums[i]
            + count * avgI * avgJ;
      }
    }
    return covMatrix;
  }
}