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

import java.io.IOException;
import org.scaleborn.linereg.sampling.Sampling.CoefficientLinearTermSampling;
import org.scaleborn.linereg.sampling.Sampling.CoefficientSquareTermSampling;
import org.scaleborn.linereg.sampling.Sampling.SamplingContext;
import org.scaleborn.linereg.sampling.io.StateInputStream;
import org.scaleborn.linereg.sampling.io.StateOutputStream;

/**
 * Created by mbok on 31.03.17.
 */
public interface SlopeCoefficientsSampling<Z extends SlopeCoefficientsSampling<Z>> extends
    SamplingContext<Z>,
    CoefficientLinearTermSampling<Z>,
    CoefficientSquareTermSampling<Z> {

  class SlopeCoefficientsSamplingProxy<Y extends SlopeCoefficientsSamplingProxy<Y>> implements
      SlopeCoefficientsSampling<Y> {

    private SamplingContext samplingContext;
    private CoefficientLinearTermSampling coefficientLinearTermSampling;
    private CoefficientSquareTermSampling coefficientSquareTermSampling;

    public SlopeCoefficientsSamplingProxy(final SamplingContext<?> samplingContext,
        final CoefficientLinearTermSampling<?> coefficientLinearTermSampling,
        final CoefficientSquareTermSampling<?> coefficientSquareTermSampling) {
      this.samplingContext = samplingContext;
      this.coefficientLinearTermSampling = coefficientLinearTermSampling;
      this.coefficientSquareTermSampling = coefficientSquareTermSampling;
    }

    @Override
    public void saveState(final StateOutputStream destination) throws IOException {
      samplingContext.saveState(destination);
      coefficientLinearTermSampling.saveState(destination);
      coefficientSquareTermSampling.saveState(destination);
    }

    @Override
    public void sample(final double[] featureValues, final double responseValue) {
      samplingContext.sample(featureValues, responseValue);
      coefficientLinearTermSampling.sample(featureValues, responseValue);
      coefficientSquareTermSampling.sample(featureValues, responseValue);
    }

    @Override
    public void loadState(final StateInputStream source) throws IOException {
      samplingContext.loadState(source);
      coefficientLinearTermSampling.loadState(source);
      coefficientSquareTermSampling.loadState(source);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void merge(final SlopeCoefficientsSamplingProxy fromSample) {
      samplingContext.merge(fromSample.samplingContext);
      coefficientLinearTermSampling.merge(fromSample.coefficientLinearTermSampling);
      coefficientSquareTermSampling.merge(fromSample.coefficientSquareTermSampling);
    }

    @Override
    public double[] getFeaturesResponseCovariance() {
      return coefficientLinearTermSampling.getFeaturesResponseCovariance();
    }

    @Override
    public double[][] getCovarianceLowerTriangularMatrix() {
      return coefficientSquareTermSampling.getCovarianceLowerTriangularMatrix();
    }

    @Override
    public long getCount() {
      return samplingContext.getCount();
    }

    @Override
    public int getFeaturesCount() {
      return samplingContext.getFeaturesCount();
    }
  }

}
