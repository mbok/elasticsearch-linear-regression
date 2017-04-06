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

package org.scaleborn.linereg.sampling;

import org.scaleborn.linereg.sampling.io.Stateful;

/**
 * Created by mbok on 26.03.17.
 */
public interface Sampling<Z extends Sampling<Z>> extends Stateful {

  void sample(double[] featureValues, double responseValue);

  void merge(Z fromSample);

  interface SamplingContext<Z extends SamplingContext<Z>> extends Sampling<Z> {

    long getCount();

    int getFeaturesCount();
  }

  interface ResponseVarianceTermSampling<Z extends ResponseVarianceTermSampling<Z>> extends
      Sampling<Z> {

    double getResponseVariance();
  }

  interface CoefficientLinearTermSampling<Z extends CoefficientLinearTermSampling<Z>> extends
      Sampling<Z> {

    /**
     * Features response covariance.
     */
    double[] getFeaturesResponseCovariance();
  }

  interface CoefficientSquareTermSampling<Z extends CoefficientSquareTermSampling<Z>> extends
      Sampling<Z> {

    double[][] getCovarianceLowerTriangularMatrix();

  }
}
