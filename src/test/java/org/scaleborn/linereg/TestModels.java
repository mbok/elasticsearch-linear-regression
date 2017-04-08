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

import java.util.ArrayList;
import java.util.List;
import org.scaleborn.linereg.calculation.statistics.Statistics;
import org.scaleborn.linereg.calculation.statistics.Statistics.DefaultStatistics;
import org.scaleborn.linereg.calculation.statistics.StatsModel;
import org.scaleborn.linereg.calculation.statistics.StatsSampling;
import org.scaleborn.linereg.calculation.statistics.StatsSampling.StatsSamplingProxy;
import org.scaleborn.linereg.evaluation.DerivationEquation;
import org.scaleborn.linereg.evaluation.DerivationEquationBuilder;
import org.scaleborn.linereg.evaluation.SlopeCoefficients;
import org.scaleborn.linereg.evaluation.commons.CommonsMathSolver;
import org.scaleborn.linereg.sampling.exact.ExactModelSamplingFactory;
import org.scaleborn.linereg.sampling.exact.ExactSamplingContext;

/**
 * Created by mbok on 19.03.17.
 */
public class TestModels {

  /**
   * Test model for verification.
   */
  public static class TestModel {

    private final int buckets;
    private final int featureCount;
    final double[][] observations;
    private final Statistics expectedStatistics;
    private final double[] expectedSlopeCoefficients;
    private StatsSampling statsSampling;
    private DerivationEquation equation;

    public TestModel(final int buckets, final int featureCount, final double[][] observations,
        final double[] expectedSlopeCoefficients,
        final Statistics statistics) {
      this.buckets = buckets;
      this.featureCount = featureCount;
      this.observations = observations;
      this.expectedStatistics = statistics;
      this.expectedSlopeCoefficients = expectedSlopeCoefficients;

      sample();
    }

    private StatsSampling<?> createSampling() {
      final ExactModelSamplingFactory f = new ExactModelSamplingFactory();
      final ExactSamplingContext samplingContext = f.createContext(this.featureCount);
      return new StatsSamplingProxy(samplingContext, f
          .createResponseVarianceTermSampling(samplingContext), f
          .createCoefficientLinearTermSampling(samplingContext), f
          .createCoefficientSquareTermSampling(samplingContext));
    }

    private void sample() {
      final List<StatsSampling<?>> bucketSamplings = new ArrayList<>(this.buckets);
      for (int i = 0; i < this.buckets; i++) {
        bucketSamplings.add(createSampling());
      }
      int b = 0;
      for (int i = 0; i < this.observations.length; i++) {
        bucketSamplings.get(b).sample(
            this.observations[i], this.observations[i][this.observations[i].length - 1]);
        b++;
        if (b >= this.buckets) {
          b = 0;
        }
      }
      this.statsSampling = createSampling();
      for (int i = 0; i < this.buckets; i++) {
        this.statsSampling.merge(bucketSamplings.get(i));
      }
      this.equation = new DerivationEquationBuilder()
          .buildDerivationEquation(this.statsSampling);
    }

    public double[] getExpectedSlopeCoefficients() {
      return this.expectedSlopeCoefficients;
    }

    public Statistics getExpectedStatistics() {
      return this.expectedStatistics;
    }

    public DerivationEquation getEquation() {
      return this.equation;
    }

    public StatsModel evaluateModel() {
      final SlopeCoefficients coefficients = new CommonsMathSolver().solveCoefficients(
          this.equation);
      return new StatsModel(this.statsSampling, coefficients);
    }

    public void assertCoefficients(final double[] givenCoefficients, final double delta) {
      assertEquals("Count of evaluated coefficients not equal",
          this.expectedSlopeCoefficients.length, givenCoefficients.length);
      for (int i = 0; i < this.expectedSlopeCoefficients.length; i++) {
        assertEquals("Evaluated coefficient " + i + " not eqal",
            this.expectedSlopeCoefficients[i], givenCoefficients[i], delta);
      }
    }

    public void assertStatistics(final Statistics statistics) {
      assertEquals("RSS not equal", this.expectedStatistics.getRss(), statistics.getRss(), 0.0001d);
      assertEquals("MSE not equal", this.expectedStatistics.getMse(), statistics.getMse(), 0.0001d);
    }
  }

  public static TestModel SIMPLE_MODEL_1 = new TestModel(2, 1, new double[][]{
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

  public static TestModel MULTI_FEATURES_2_MODEL_1 = new TestModel(3, 2, new double[][]{
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

  public static TestModel MULTI_FEATURES_3_MODEL_1 = new TestModel(4, 3, new double[][]{
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

  /**
   * Reference data set Longley: from http://www.itl.nist.gov/div898/strd/lls/data/Longley.shtml
   */
  public static TestModel MULTI_FEATURES_6_LONGLEY = new TestModel(1, 6,
      new double[][]{
          new double[]{83.0, 234289, 2356, 1590, 107608, 1947, 60323},
          new double[]{88.5, 259426, 2325, 1456, 108632, 1948, 61122},
          new double[]{88.2, 258054, 3682, 1616, 109773, 1949, 60171},
          new double[]{89.5, 284599, 3351, 1650, 110929, 1950, 61187},
          new double[]{96.2, 328975, 2099, 3099, 112075, 1951, 63221},
          new double[]{98.1, 346999, 1932, 3594, 113270, 1952, 63639},
          new double[]{99.0, 365385, 1870, 3547, 115094, 1953, 64989},
          new double[]{100.0, 363112, 3578, 3350, 116219, 1954, 63761},
          new double[]{101.2, 397469, 2904, 3048, 117388, 1955, 66019},
          new double[]{104.6, 419180, 2822, 2857, 118734, 1956, 67857},
          new double[]{108.4, 442769, 2936, 2798, 120445, 1957, 68169},
          new double[]{110.8, 444546, 4681, 2637, 121950, 1958, 66513},
          new double[]{112.6, 482704, 3813, 2552, 123366, 1959, 68655},
          new double[]{114.2, 502601, 3931, 2514, 125368, 1960, 69564},
          new double[]{115.7, 518173, 4806, 2572, 127852, 1961, 69331},
          new double[]{116.9, 554894, 4007, 2827, 130081, 1962, 70551}
      }
      /**
       *      number of observations: 16
       *      beta(0)	  -3482258.63459582	     890420.383607373
       *      beta(1)	     15.0618722713733	     84.9149257747669
       *      beta(2)	  -0.358191792925910E-01	     0.334910077722432E-01
       *      beta(3)	  -2.02022980381683	     0.488399681651699
       *      beta(4)	  -1.03322686717359	     0.214274163161675
       *      beta(5)	  -0.511041056535807E-01	     0.226073200069370
       *      beta(6)	     1829.15146461355	     455.478499142212
       *      Residual Sums of Squares 836424.055505915
       *      R-Squared	     0.995479004577296
       */
      , new double[]{15.0618722713733d, -0.358191792925910E-01d, -2.02022980381683d,
      -1.03322686717359d, -0.511041056535807E-01d, 1829.15146461355d},
      new DefaultStatistics(836424.055505915d, 836424.055505915d / 16));

}
