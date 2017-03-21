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

package org.scaleborn.linereg.statistics;

import org.scaleborn.linereg.Model;
import org.scaleborn.linereg.sampling.SampledData;

/**
 * Created by mbok on 19.03.17.
 */
public class StatsBuilder {

  Statistics buildStats(final Model<SampledData> model) {
    final SampledData sampledData = model.getSampledData();
    int featuresCount = sampledData.getFeaturesCount();
    double squaredError =
        sampledData.getTargetSquareSum() - (2 * (sampledData.getTargetAverage() * sampledData
            .getTargetSum()));

    double[] deConstraints = model.getDerivationEquation().getConstraints();
    double[][] deMatrix = model.getDerivationEquation().getCovarianceLowerTriangularMatrix();
    double[] slopeCoefficients = model.getSlopeCoefficients();
    for (int i = 0; i < featuresCount; i++) {
      double c = slopeCoefficients[i];
      double c2 = c * c;
      // Add values from derivation equation constraint
      // Double and negate, because the derivation equation is normalized
      squaredError -= 2 * deConstraints[i] * c;

      // Add values from covariance matrix of the derivation matrix
      for (int j = 0; j <= i; j++) {
        if (i == j) {
          // Variance term
          squaredError += c2 * deMatrix[i][j];
        } else {
          // Covariance term
          squaredError += 2 * c * slopeCoefficients[j] * deMatrix[i][j];
        }
      }
    }
    double ic = sampledData.getTargetSum();
    ic *= ic;
    squaredError += ic / sampledData.getCount();
    final double rss = squaredError;
    return new Statistics() {
      @Override
      public double getRss() {
        return rss;
      }

      @Override
      public double getMse() {
        return rss / sampledData.getCount();
      }
    };
  }
}
