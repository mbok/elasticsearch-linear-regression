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
import org.scaleborn.linereg.sampling.Sampling.CoefficientSquareTermSampling;
import org.scaleborn.linereg.sampling.io.StateInputStream;
import org.scaleborn.linereg.sampling.io.StateOutputStream;

/**
 * Samples square term data with covariance matrix solved exactly. Doing this that
 * way result in a time complexity of O(C² * N) and a memory consumption of O(C²), where C is
 * the count of variables and N the count of observations / documents.
 * Created by mbok on 27.03.17.
 */
public class ExactCoefficientSquareTermSampling implements
    CoefficientSquareTermSampling<ExactCoefficientSquareTermSampling> {

  /**
   * TODO: Migrate to another algorithm to avoid sums of products, which can lead to numerical
   * instability as well as to arithmetic overflow.
   */
  private double[][] featuresProductSums;
  private ExactSamplingContext context;

  public ExactCoefficientSquareTermSampling(ExactSamplingContext context) {
    this.context = context;
    int featuresCount = context.getFeaturesCount();
    this.featuresProductSums = new double[featuresCount][];
    for (int i = 0; i < featuresCount; i++) {
      this.featuresProductSums[i] = new double[featuresCount];
    }
  }

  @Override
  public double[][] getCovarianceLowerTriangularMatrix() {
    int featuresCount = this.context.getFeaturesCount();
    long count = this.context.getCount();
    double[][] covMatrix = new double[featuresCount][];
    double[] averages = this.context.getFeaturesMean();
    double[] featureSums = this.context.featureSums;
    for (int i = 0; i < featuresCount; i++) {
      double avgI = averages[i];
      covMatrix[i] = new double[featuresCount];
      // Iterate until "i" due to the covariance matrix is symmetric and
      // build only the lower triangle
      for (int j = 0; j <= i; j++) {
        double avgJ = averages[j];
        covMatrix[i][j] =
            this.featuresProductSums[i][j] - avgI * featureSums[j] - avgJ * featureSums[i]
                + count * avgI * avgJ;
      }
    }
    return covMatrix;
  }

  @Override
  public void sample(final double[] featureValues, final double responseValue) {
    int featuresCount = this.context.getFeaturesCount();
    for (int i = 0; i < featuresCount; i++) {
      double vi = featureValues[i];
      for (int j = 0; j < featuresCount; j++) {
        this.featuresProductSums[i][j] += vi * featureValues[j];
      }
    }
  }

  @Override
  public void merge(final ExactCoefficientSquareTermSampling fromSample) {
    int featuresCount = this.context.getFeaturesCount();
    for (int i = 0; i < featuresCount; i++) {
      for (int j = 0; j < featuresCount; j++) {
        this.featuresProductSums[i][j] += fromSample.featuresProductSums[i][j];
      }
    }
  }

  @Override
  public void saveState(final StateOutputStream stream) throws IOException {
    stream.writeDoubleMatrix(this.featuresProductSums);
  }

  @Override
  public void loadState(final StateInputStream stream) throws IOException {
    this.featuresProductSums = stream.readDoubleMatrix();
  }

  @Override
  public String toString() {
    return "ExactCoefficientSquareTermSampling{" +
        "featuresProductSums=" + Arrays.deepToString(this.featuresProductSums) +
        '}';
  }
}
