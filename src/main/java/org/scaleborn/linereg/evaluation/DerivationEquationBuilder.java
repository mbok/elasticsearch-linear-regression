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

package org.scaleborn.linereg.evaluation;

import org.scaleborn.linereg.sampling.SampledData;

/**
 * Created by mbok on 17.03.17.
 */
public class DerivationEquationBuilder {

  public DerivationEquation buildDerivationEquation(final SampledData sampledData) {
    final int featuresCount = sampledData.getFeaturesCount();
    final double[] averages = sampledData.getFeatureAverages();
    final double targetSum = sampledData.getTargetSum();
    final double[] featureTargetProductSums = sampledData.getFeatureTargetProductSums();

    // Vector
    final double[] eqVector = new double[featuresCount];
    for (int i = 0; i < featuresCount; i++) {
      // For the least square error calculation we would multiply by 2 here, but
      // we ignore it here due to DerivationEquation expects both the vector
      // and the matrix without multiplication by 2.
      eqVector[i] = featureTargetProductSums[i] - averages[i] * targetSum;
    }

    // The partial derivation of the least square sampled linear model results in a matrix equal
    // to the "covariance matrix" * 2. Due to DerivationEquation expects both the vector
    // and the matrix without multiplication by 2, we can pass it through.
    return new DerivationEquation() {
      @Override
      public double[][] getCovarianceLowerTriangularMatrix() {
        return sampledData.getCovarianceLowerTriangularMatrix();
      }

      @Override
      public double[] getConstraints() {
        return eqVector;
      }
    };
  }
}
