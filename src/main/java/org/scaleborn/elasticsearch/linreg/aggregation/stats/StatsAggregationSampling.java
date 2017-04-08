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

package org.scaleborn.elasticsearch.linreg.aggregation.stats;

import java.io.IOException;
import org.scaleborn.elasticsearch.linreg.aggregation.support.BaseSampling;
import org.scaleborn.linereg.calculation.statistics.StatsSampling;
import org.scaleborn.linereg.sampling.io.StateInputStream;
import org.scaleborn.linereg.sampling.io.StateOutputStream;

/**
 * Created by mbok on 08.04.17.
 */
public class StatsAggregationSampling extends BaseSampling<StatsAggregationSampling> implements
    StatsSampling<StatsAggregationSampling> {

  private final ResponseVarianceTermSampling responseVarianceTermSampling;

  public StatsAggregationSampling(
      final SamplingContext<?> samplingContext,
      final CoefficientLinearTermSampling<?> coefficientLinearTermSampling,
      final CoefficientSquareTermSampling<?> coefficientSquareTermSampling,
      final InterceptSampling<?> interceptSampling,
      final ResponseVarianceTermSampling<?> responseVarianceTermSampling) {
    super(samplingContext, coefficientLinearTermSampling, coefficientSquareTermSampling,
        interceptSampling);
    this.responseVarianceTermSampling = responseVarianceTermSampling;
  }

  @Override
  public void merge(final StatsAggregationSampling fromSample) {
    super.merge(fromSample);
    this.responseVarianceTermSampling.merge(fromSample.responseVarianceTermSampling);
  }

  @Override
  public void sample(final double[] featureValues, final double responseValue) {
    super.sample(featureValues, responseValue);
    this.responseVarianceTermSampling.sample(featureValues, responseValue);
  }

  @Override
  public void saveState(final StateOutputStream destination) throws IOException {
    super.saveState(destination);
    this.responseVarianceTermSampling.saveState(destination);
  }

  @Override
  public void loadState(final StateInputStream source) throws IOException {
    super.loadState(source);
    this.responseVarianceTermSampling.loadState(source);
  }

  @Override
  public double getResponseVariance() {
    return this.responseVarianceTermSampling.getResponseVariance();
  }
}
