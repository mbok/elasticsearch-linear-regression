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

import java.util.Arrays;
import org.scaleborn.linereg.sampling.CoefficientLinearTerm;
import org.scaleborn.linereg.sampling.CoefficientSquareTerm;
import org.scaleborn.linereg.sampling.ResponseVarianceTerm;
import org.scaleborn.linereg.sampling.SamplingContext;

/**
 * Bean for evaluated linear model fitting best the sampled data regarding
 * least square approach.
 *
 * Created by mbok on 19.03.17.
 */
public class StatsModel {

  private SamplingContext<?> samplingContext;
  private ResponseVarianceTerm responseVarianceTerm;
  private CoefficientLinearTerm coefficientLinearTerm;
  private CoefficientSquareTerm coefficientSquareTerm;
  private double[] slopeCoefficients;

  public StatsModel(final SamplingContext<?> samplingContext,
      final ResponseVarianceTerm responseVarianceTerm,
      final CoefficientLinearTerm coefficientLinearTerm,
      final CoefficientSquareTerm coefficientSquareTerm, final double[] slopeCoefficients) {
    this.samplingContext = samplingContext;
    this.responseVarianceTerm = responseVarianceTerm;
    this.coefficientLinearTerm = coefficientLinearTerm;
    this.coefficientSquareTerm = coefficientSquareTerm;
    this.slopeCoefficients = slopeCoefficients;
  }

  public SamplingContext<?> getSamplingContext() {
    return samplingContext;
  }

  public ResponseVarianceTerm getResponseVarianceTerm() {
    return responseVarianceTerm;
  }

  public CoefficientLinearTerm getCoefficientLinearTerm() {
    return coefficientLinearTerm;
  }

  public CoefficientSquareTerm getCoefficientSquareTerm() {
    return coefficientSquareTerm;
  }

  public double[] getSlopeCoefficients() {
    return slopeCoefficients;
  }

  @Override
  public String toString() {
    return "StatsModel{" +
        "samplingContext=" + samplingContext +
        ", responseVarianceTerm=" + responseVarianceTerm +
        ", coefficientLinearTerm=" + coefficientLinearTerm +
        ", coefficientSquareTerm=" + coefficientSquareTerm +
        ", slopeCoefficients=" + Arrays.toString(slopeCoefficients) +
        '}';
  }
}
