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

package org.scaleborn.linereg.sampling.exact;

import org.scaleborn.linereg.sampling.ModelSamplerFactory;
import org.scaleborn.linereg.sampling.Sampler.CoefficientLinearTermSampler;
import org.scaleborn.linereg.sampling.Sampler.CoefficientSquareTermSampler;
import org.scaleborn.linereg.sampling.Sampler.ResponseVarianceTermSampler;

/**
 * Created by mbok on 26.03.17.
 */
public class ExactModelSamplerFactory implements ModelSamplerFactory<ExactSamplingContext> {

  @Override
  public ExactSamplingContext createContext(final int featuresCount) {
    return new ExactSamplingContext(featuresCount);
  }

  @Override
  public ResponseVarianceTermSampler<?> createResponseVarianceTermSampler(
      final ExactSamplingContext context) {
    return new ExactResponseVarianceTermSampler(context);
  }

  private static class ExactResponseVarianceTermSampler implements
      ResponseVarianceTermSampler<ExactResponseVarianceTermSampler> {

    private ExactSamplingContext context;

    public ExactResponseVarianceTermSampler(
        final ExactSamplingContext context) {
      this.context = context;
    }

    @Override
    public double getResponseVariance() {
      return context.responseSquareSum
          - context.responseSum / context.getCount() * context.responseSum;
    }

    @Override
    public void sample(final double[] featureValues, final double responseValue) {
      // Nothing to sample, covered by ExactSamplingContext
    }

    @Override
    public void merge(final ExactResponseVarianceTermSampler fromSample) {
      // Nothing to merge, covered by ExactSamplingContext
    }
  }

  @Override
  public CoefficientLinearTermSampler<?> createCoefficientLinearTermSampler(
      final ExactSamplingContext context) {
    return new ExactCoefficientLinearTermSampler(context);
  }

  private static class ExactCoefficientLinearTermSampler implements
      CoefficientLinearTermSampler<ExactCoefficientLinearTermSampler> {

    private ExactSamplingContext context;

    public ExactCoefficientLinearTermSampler(
        final ExactSamplingContext context) {
      this.context = context;
    }

    @Override
    public double[] getFeaturesResponseCovariance() {
      /**
       * TODO: Migrate to another algorithm to avoid sums of products, which can lead to numerical
       * instability as well as to arithmetic overflow.
       */
      long count = context.getCount();
      int featuresCount = context.getFeaturesCount();
      double[] covariance = new double[context.getFeaturesCount()];
      double[] featuresMean = context.getFeaturesMean();
      double responseMean = context.getResponseMean();
      for (int i = 0; i < featuresCount; i++) {
        covariance[i] =
            context.featuresResponseProductSum[i] - featuresMean[i] * context.responseSum
                - responseMean * context.featureSums[i] + count * featuresMean[i] * responseMean;
      }
      return covariance;
    }

    @Override
    public void sample(final double[] featureValues, final double responseValue) {
      // Nothing to sample, covered by ExactSamplingContext
    }

    @Override
    public void merge(final ExactCoefficientLinearTermSampler fromSample) {
      // Nothing to merge, covered by ExactSamplingContext
    }
  }

  @Override

  public CoefficientSquareTermSampler<?> createCoefficientSquareTermSampler(
      final ExactSamplingContext context) {
    return new ExactCoefficientSquareTermSampler(context);
  }
}
