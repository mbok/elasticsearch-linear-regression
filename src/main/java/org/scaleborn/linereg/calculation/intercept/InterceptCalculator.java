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

package org.scaleborn.linereg.calculation.intercept;

import org.scaleborn.linereg.estimation.SlopeCoefficients;
import org.scaleborn.linereg.sampling.Sampling.InterceptSampling;
import org.scaleborn.linereg.sampling.Sampling.SamplingContext;

/**
 * Calculates the intercept coefficient Q0 for the linear function:
 * h(X) = Q0 + Q1*X1 + Q2*X2 ... + Qc*Xc
 * Created by mbok on 07.04.17.
 */
public class InterceptCalculator {

  public double calculate(final SlopeCoefficients slopeCoefficients,
      final SamplingContext<?> samplingContext,
      final InterceptSampling<?> interceptSampling) {
    double intercept = 0;
    final double[] featuresMean = interceptSampling.getFeaturesMean();
    final int featuresCount = samplingContext.getFeaturesCount();
    final double[] coefficients = slopeCoefficients.getCoefficients();
    for (int i = 0; i < featuresCount; i++) {
      intercept += featuresMean[i] * coefficients[i];
    }
    return interceptSampling.getResponseMean() - intercept;
  }

}
