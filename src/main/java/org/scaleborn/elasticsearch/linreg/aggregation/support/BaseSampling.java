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

package org.scaleborn.elasticsearch.linreg.aggregation.support;

import java.io.IOException;
import org.scaleborn.linereg.evaluation.SlopeCoefficientsSampling.SlopeCoefficientsSamplingProxy;
import org.scaleborn.linereg.sampling.Sampling.InterceptSampling;
import org.scaleborn.linereg.sampling.io.StateInputStream;
import org.scaleborn.linereg.sampling.io.StateOutputStream;

/**
 * Created by mbok on 08.04.17.
 */
public class BaseSampling<S extends BaseSampling<S>> extends
    SlopeCoefficientsSamplingProxy<S> implements InterceptSampling<S> {

  private final InterceptSampling interceptSampling;

  public BaseSampling(
      final SamplingContext<?> samplingContext,
      final CoefficientLinearTermSampling<?> coefficientLinearTermSampling,
      final CoefficientSquareTermSampling<?> coefficientSquareTermSampling,
      final InterceptSampling<?> interceptSampling) {
    super(samplingContext, coefficientLinearTermSampling, coefficientSquareTermSampling);
    this.interceptSampling = interceptSampling;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void merge(final S fromSample) {
    super.merge(fromSample);
    this.interceptSampling.merge(((BaseSampling) fromSample).interceptSampling);
  }

  @Override
  public void sample(final double[] featureValues, final double responseValue) {
    super.sample(featureValues, responseValue);
    this.interceptSampling.sample(featureValues, responseValue);
  }

  @Override
  public void saveState(final StateOutputStream destination) throws IOException {
    super.saveState(destination);
    this.interceptSampling.saveState(destination);
  }

  @Override
  public void loadState(final StateInputStream source) throws IOException {
    super.loadState(source);
    this.interceptSampling.loadState(source);
  }

  @Override
  public double[] getFeaturesMean() {
    return this.interceptSampling.getFeaturesMean();
  }

  @Override
  public double getResponseMean() {
    return this.interceptSampling.getResponseMean();
  }
}
