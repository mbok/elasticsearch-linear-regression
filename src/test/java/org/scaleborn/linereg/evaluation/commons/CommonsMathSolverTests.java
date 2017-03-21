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

import org.elasticsearch.test.ESTestCase;
import org.junit.Test;
import org.scaleborn.linereg.TestModels;
import org.scaleborn.linereg.TestModels.TestModel;
import org.scaleborn.linereg.evaluation.DerivationEquation;
import org.scaleborn.linereg.evaluation.DerivationEquationBuilder;

/**
 * Created by mbok on 18.03.17.
 */
public class CommonsMathSolverTests extends ESTestCase {

  /**
   * Tests coefficient evaluation with one feature variable.
   */
  @Test
  public void testSimpleRegression() {
    TestModel model = TestModels.SIMPLE_MODEL_1;
    DerivationEquation equation = new DerivationEquationBuilder()
        .buildDerivationEquation(model.getSampledData());
    double[] coefficients = new CommonsMathSolver().solveCoefficients(equation);
    logger.info("Evaluated linreg coefficients: {}", coefficients);
    model.assertCoefficients(coefficients, 0.0000001);
  }

  /**
   * Tests coefficient evaluation with two feature variables.
   */
  @Test
  public void testMultipleRegressionWith2Features() {
    TestModel testModel = TestModels.MULTI_FEATURES_2_MODEL_1;
    DerivationEquation equation = new DerivationEquationBuilder()
        .buildDerivationEquation(testModel.getSampledData());
    double[] coefficients = new CommonsMathSolver().solveCoefficients(equation);
    logger.info("Evaluated linreg coefficients: {}", coefficients);
    testModel.assertCoefficients(coefficients, 0.0000001);
  }

  /**
   * Tests coefficient evaluation with three feature variables.
   */
  @Test
  public void testMultipleRegressionWith3Features() {
    TestModel testModel = TestModels.MULTI_FEATURES_3_MODEL_1;
    DerivationEquation equation = new DerivationEquationBuilder()
        .buildDerivationEquation(testModel.getSampledData());
    double[] coefficients = new CommonsMathSolver().solveCoefficients(equation);
    logger.info("Evaluated linreg coefficients: {}", coefficients);
    testModel.assertCoefficients(coefficients, 0.0000001);
  }
}
