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

package org.scaleborn.linereg;

import static org.junit.Assert.assertEquals;
import static org.scaleborn.linereg.sampling.SamplingUtil.fillWithObservations;

import org.scaleborn.linereg.evaluation.DerivationEquation;
import org.scaleborn.linereg.evaluation.DerivationEquationBuilder;
import org.scaleborn.linereg.evaluation.commons.CommonsMathSolver;
import org.scaleborn.linereg.sampling.Sampler;
import org.scaleborn.linereg.sampling.Sampler.CoefficientLinearTermSampler;
import org.scaleborn.linereg.sampling.Sampler.CoefficientSquareTermSampler;
import org.scaleborn.linereg.sampling.Sampler.ResponseVarianceTermSampler;
import org.scaleborn.linereg.sampling.SamplerBuilder;
import org.scaleborn.linereg.sampling.exact.ExactModelSamplerFactory;
import org.scaleborn.linereg.sampling.exact.ExactSamplingContext;
import org.scaleborn.linereg.statistics.Statistics;
import org.scaleborn.linereg.statistics.Statistics.DefaultStatistics;
import org.scaleborn.linereg.statistics.StatsModel;

/**
 * Created by mbok on 19.03.17.
 */
public class TestModels {

  /**
   * Test model for verification.
   */
  public static class TestModel {

    private int featureCount;
    final double[][] observations;
    private Statistics expectedStatistics;
    private double[] expectedSlopeCoefficients;
    private ExactSamplingContext samplingContext;
    private ResponseVarianceTermSampler<?> responseVarianceTermSampler;
    private CoefficientLinearTermSampler<?> coefficientLinearTermSampler;
    private CoefficientSquareTermSampler<?> coefficientSquareTermSampler;
    private DerivationEquation equation;

    public TestModel(final int featureCount, final double[][] observations,
        double[] expectedSlopeCoefficients,
        Statistics statistics) {
      this.featureCount = featureCount;
      this.observations = observations;
      this.expectedStatistics = statistics;
      this.expectedSlopeCoefficients = expectedSlopeCoefficients;

      sample();
    }

    private void sample() {
      ExactModelSamplerFactory f = new ExactModelSamplerFactory();
      samplingContext = f.createContext(featureCount);
      responseVarianceTermSampler = f
          .createResponseVarianceTermSampler(samplingContext);
      coefficientLinearTermSampler = f
          .createCoefficientLinearTermSampler(samplingContext);
      coefficientSquareTermSampler = f
          .createCoefficientSquareTermSampler(samplingContext);
      Sampler<?> sampler = SamplerBuilder.forContext(samplingContext)
          .addSampler(responseVarianceTermSampler).addSampler(coefficientLinearTermSampler)
          .addSampler(coefficientSquareTermSampler).build();
      fillWithObservations(sampler, observations);
      equation = new DerivationEquationBuilder()
          .buildDerivationEquation(coefficientLinearTermSampler, coefficientSquareTermSampler);
    }

    public double[] getExpectedSlopeCoefficients() {
      return expectedSlopeCoefficients;
    }

    public Statistics getExpectedStatistics() {
      return expectedStatistics;
    }

    public DerivationEquation getEquation() {
      return equation;
    }

    public StatsModel evaluateModel() {
      double[] coefficients = new CommonsMathSolver().solveCoefficients(equation);
      return new StatsModel(samplingContext, responseVarianceTermSampler,
          coefficientLinearTermSampler,
          coefficientSquareTermSampler, coefficients);
    }

    public void assertCoefficients(double[] givenCoefficients, double delta) {
      assertEquals("Count of evaluated coefficients not equal",
          expectedSlopeCoefficients.length, givenCoefficients.length);
      for (int i = 0; i < expectedSlopeCoefficients.length; i++) {
        assertEquals("Evaluated coefficient " + i + " not eqal",
            expectedSlopeCoefficients[i], givenCoefficients[i], delta);
      }
    }

    public void assertStatistics(final Statistics statistics) {
      assertEquals("RSS not equal", expectedStatistics.getRss(), statistics.getRss(), 0.00001d);
      assertEquals("MSE not equal", expectedStatistics.getMse(), statistics.getMse(), 0.00001d);
    }
  }

  public static TestModel SIMPLE_MODEL_1 = new TestModel(1, new double[][]{
      new double[]{-2, 5},
      new double[]{1, 3},
      new double[]{4, 1},
      new double[]{3, 0},
      new double[]{2, 0},
      new double[]{0, 2},
      new double[]{5, -1}
  }, new double[]{-0.762295082d},
      /**
       * Correlation Coefficient: r = -8.875274182·10-1
       * Residual Sum of Squares: rss = 5.459016393
       * Coefficient of Determination: R2 = 0.787704918
       */
      new DefaultStatistics(5.459016393, 5.459016393 / 7));

  public static TestModel MULTI_FEATURES_2_MODEL_1 = new TestModel(2, new double[][]{
      new double[]{-2, 3, 5},
      new double[]{1, 1, 3},
      new double[]{4, 2, 1},
      new double[]{3, 0, 0},
      new double[]{2, -2, 0},
      new double[]{0, 0, 2},
      new double[]{5, -5, -1}
      // Expected results evaluated by http://www.xuru.org/rt/MLR.asp
      /**
       * Residual Sum of Squares: rss = 2.99513878
       * Coefficient of Determination: R2 = 8.835223808·10-1
      */
  }, new double[]{-0.5496314882d, 0.3070409283d},
      new DefaultStatistics(2.99513878d, 2.99513878d / 7));

  public static TestModel MULTI_FEATURES_3_MODEL_1 = new TestModel(3, new double[][]{
      new double[]{4, -2, 3, 5},
      new double[]{7, 1, 1, 3},
      new double[]{2, 4, 2, 1},
      new double[]{-5, 3, 0, 0},
      new double[]{5, 2, -2, 0},
      new double[]{20, 0, 0, 2},
      new double[]{-9, 5, -5, -1}
      // Expected results evaluated by http://www.xuru.org/rt/MLR.asp
      /**
       * Residual Sum of Squares: rss = 2.705920002
       * Coefficient of Determination: R2 = 8.947697777·10-1
      */
  }, new double[]{-0.03116979852d, -0.6272993725d, 0.3079647314d},
      new DefaultStatistics(2.705920002d, 2.705920002d / 7));
}
