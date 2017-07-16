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

package org.scaleborn.linereg.estimation;

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

    private final SamplingContext samplingContext;
    private final CoefficientLinearTermSampling coefficientLinearTermSampling;
    private final CoefficientSquareTermSampling coefficientSquareTermSampling;

    public SlopeCoefficientsSamplingProxy(final SamplingContext<?> samplingContext,
        final CoefficientLinearTermSampling<?> coefficientLinearTermSampling,
        final CoefficientSquareTermSampling<?> coefficientSquareTermSampling) {
      this.samplingContext = samplingContext;
      this.coefficientLinearTermSampling = coefficientLinearTermSampling;
      this.coefficientSquareTermSampling = coefficientSquareTermSampling;
    }

    @Override
    public void saveState(final StateOutputStream destination) throws IOException {
      this.samplingContext.saveState(destination);
      this.coefficientLinearTermSampling.saveState(destination);
      this.coefficientSquareTermSampling.saveState(destination);
    }

    @Override
    public void sample(final double[] featureValues, final double responseValue) {
      this.samplingContext.sample(featureValues, responseValue);
      this.coefficientLinearTermSampling.sample(featureValues, responseValue);
      this.coefficientSquareTermSampling.sample(featureValues, responseValue);
    }

    @Override
    public void loadState(final StateInputStream source) throws IOException {
      this.samplingContext.loadState(source);
      this.coefficientLinearTermSampling.loadState(source);
      this.coefficientSquareTermSampling.loadState(source);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void merge(final Y fromSample) {
      this.samplingContext.merge(((SlopeCoefficientsSamplingProxy<Y>) fromSample).samplingContext);
      this.coefficientLinearTermSampling
          .merge(((SlopeCoefficientsSamplingProxy<Y>) fromSample).coefficientLinearTermSampling);
      this.coefficientSquareTermSampling
          .merge(((SlopeCoefficientsSamplingProxy<Y>) fromSample).coefficientSquareTermSampling);
    }

    @Override
    public double[] getFeaturesResponseCovariance() {
      return this.coefficientLinearTermSampling.getFeaturesResponseCovariance();
    }

    @Override
    public double[][] getCovarianceLowerTriangularMatrix() {
      return this.coefficientSquareTermSampling.getCovarianceLowerTriangularMatrix();
    }

    @Override
    public long getCount() {
      return this.samplingContext.getCount();
    }

    @Override
    public int getFeaturesCount() {
      return this.samplingContext.getFeaturesCount();
    }

    @Override
    public String toString() {
      return "SlopeCoefficientsSamplingProxy{" +
          "samplingContext=" + this.samplingContext +
          ", coefficientLinearTermSampling=" + this.coefficientLinearTermSampling +
          ", coefficientSquareTermSampling=" + this.coefficientSquareTermSampling +
          '}';
    }
  }

}
