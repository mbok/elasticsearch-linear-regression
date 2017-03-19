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

package org.scaleborn.linereg.evaluation.commons;

import static org.scaleborn.linereg.evaluation.sampling.SamplingUtil.fillWithObservations;

import org.elasticsearch.test.ESTestCase;
import org.junit.Assert;
import org.junit.Test;
import org.scaleborn.linereg.evaluation.DerivationEquation;
import org.scaleborn.linereg.evaluation.DerivationEquationBuilder;
import org.scaleborn.linereg.sampling.SamplingData;
import org.scaleborn.linereg.sampling.exact.ExactSamplingData;

/**
 * Created by mbok on 18.03.17.
 */
public class CommonsMathSolverTests extends ESTestCase {

  /**
   * Tests coefficient evaluation with one feature variable.
   */
  @Test
  public void testSimpleRegression() {
    SamplingData sd = new ExactSamplingData(1);
    fillWithObservations(sd, new double[][]{
        new double[]{-2, 5},
        new double[]{1, 3},
        new double[]{4, 1},
        new double[]{3, 0},
        new double[]{2, 0},
        new double[]{0, 2},
        new double[]{5, -1}
    });
    DerivationEquation equation = new DerivationEquationBuilder()
        .buildDerivationEquation(sd);
    double[] coefficients = new CommonsMathSolver().solveCoefficients(equation);
    logger.info("Evaluated linreg coefficients: {}", coefficients);
    Assert.assertEquals(1, coefficients.length);
    Assert.assertEquals(-0.7623d, coefficients[0], 0.0001);
  }

  /**
   * Tests coefficient evaluation with two feature variables.
   */
  @Test
  public void testMultipleRegressionWith2Features() {
    SamplingData sd = new ExactSamplingData(2);
    fillWithObservations(sd, new double[][]{
        new double[]{-2, 3, 5},
        new double[]{1, 1, 3},
        new double[]{4, 2, 1},
        new double[]{3, 0, 0},
        new double[]{2, -2, 0},
        new double[]{0, 0, 2},
        new double[]{5, -5, -1}
    });
    DerivationEquation equation = new DerivationEquationBuilder()
        .buildDerivationEquation(sd);
    double[] coefficients = new CommonsMathSolver().solveCoefficients(equation);
    logger.info("Evaluated linreg coefficients: {}", coefficients);
    Assert.assertEquals(2, coefficients.length);
    // Expected results evaluated by http://www.xuru.org/rt/MLR.asp
    Assert.assertEquals(-0.5496314882d, coefficients[0], 0.0000001);
    Assert.assertEquals(0.3070409283d, coefficients[1], 0.0000001);
  }

  /**
   * Tests coefficient evaluation with three feature variables.
   */
  @Test
  public void testMultipleRegressionWith3Features() {
    SamplingData sd = new ExactSamplingData(3);
    fillWithObservations(sd, new double[][]{
        new double[]{4, -2, 3, 5},
        new double[]{7, 1, 1, 3},
        new double[]{2, 4, 2, 1},
        new double[]{-5, 3, 0, 0},
        new double[]{5, 2, -2, 0},
        new double[]{20, 0, 0, 2},
        new double[]{-9, 5, -5, -1}
    });
    DerivationEquation equation = new DerivationEquationBuilder()
        .buildDerivationEquation(sd);
    double[] coefficients = new CommonsMathSolver().solveCoefficients(equation);
    logger.info("Evaluated linreg coefficients: {}", coefficients);
    Assert.assertEquals(3, coefficients.length);
    // Expected results evaluated by http://www.xuru.org/rt/MLR.asp
    Assert.assertEquals(-0.03116979852d, coefficients[0], 0.0000001);
    Assert.assertEquals(-0.6272993725d, coefficients[1], 0.0000001);
    Assert.assertEquals(0.3079647314d, coefficients[2], 0.0000001);
  }
}
