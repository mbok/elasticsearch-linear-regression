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

import java.io.IOException;
import org.scaleborn.linereg.evaluation.SlopeCoefficientsSampling;
import org.scaleborn.linereg.sampling.Sampling.ResponseVarianceTermSampling;
import org.scaleborn.linereg.sampling.io.StateInputStream;
import org.scaleborn.linereg.sampling.io.StateOutputStream;

/**
 * Created by mbok on 31.03.17.
 */
public interface StatsSampling<Z extends StatsSampling<Z>> extends SlopeCoefficientsSampling<Z>,
    ResponseVarianceTermSampling<Z> {

  class StatsSamplingProxy<Y extends StatsSamplingProxy<Y>> extends
      SlopeCoefficientsSamplingProxy<Y> implements
      StatsSampling<Y> {

    private ResponseVarianceTermSampling responseVarianceTermSampling;

    public StatsSamplingProxy(
        final SamplingContext<?> samplingContext,
        final ResponseVarianceTermSampling<?> responseVarianceTermSampling,
        final CoefficientLinearTermSampling<?> coefficientLinearTermSampling,
        final CoefficientSquareTermSampling<?> coefficientSquareTermSampling) {
      super(samplingContext, coefficientLinearTermSampling, coefficientSquareTermSampling);
      this.responseVarianceTermSampling = responseVarianceTermSampling;
    }

    @Override
    public void saveState(final StateOutputStream destination) throws IOException {
      super.saveState(destination);
      responseVarianceTermSampling.saveState(destination);
    }

    @Override
    public void sample(final double[] featureValues, final double responseValue) {
      super.sample(featureValues, responseValue);
      responseVarianceTermSampling.sample(featureValues, responseValue);
    }

    @Override
    public void loadState(final StateInputStream source) throws IOException {
      super.loadState(source);
      responseVarianceTermSampling.loadState(source);
    }

    @Override
    public void merge(final StatsSamplingProxy fromSample) {
      super.merge(fromSample);
      responseVarianceTermSampling.merge(fromSample.responseVarianceTermSampling);
    }

    @Override
    public double getResponseVariance() {
      return responseVarianceTermSampling.getResponseVariance();
    }

    @Override
    public String toString() {
      return "StatsSamplingProxy{" +
          "responseVarianceTermSampling=" + responseVarianceTermSampling +
          "} " + super.toString();
    }
  }
}
