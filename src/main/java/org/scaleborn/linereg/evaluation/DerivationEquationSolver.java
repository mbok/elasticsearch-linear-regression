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

package org.scaleborn.linereg.evaluation;

/**
 * Solves the derivation equation and returns the coefficients for the best fit prediction
 * equation.
 * Created by mbok on 18.03.17.
 */
public interface DerivationEquationSolver {

  /**
   * Solves the derivation equation and returns the coefficients for the best fit prediction
   * equation.
   *
   * @param eq the derivation equation to solve. The input parameter should remain unchanged for
   * further calculations.
   * @return the slope coefficients (excluding the intercept) for the best fit prediction equation
   */
  double[] solveCoefficients(DerivationEquation eq);
}
