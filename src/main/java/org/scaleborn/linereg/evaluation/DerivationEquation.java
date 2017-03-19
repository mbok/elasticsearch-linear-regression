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
 * Represents the derivation equation (divided by 2) build up from the sampled data.
 * Created by mbok on 17.03.17.
 */
public interface DerivationEquation {

  /**
   * @return the covariance lower triangular matrix build from the sampled data. This is equal to
   * the partial derivation matrix divided by 2.
   */
  double[][] getCovarianceLowerTriangularMatrix();

  /**
   * @return the constraints vector of the derivation equation divided by 2.
   */
  double[] getConstraints();
}
