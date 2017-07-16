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

package org.scaleborn.linereg.estimation.commons;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.CholeskyDecomposition;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.NonPositiveDefiniteMatrixException;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.scaleborn.linereg.estimation.DerivationEquation;
import org.scaleborn.linereg.estimation.DerivationEquationSolver;
import org.scaleborn.linereg.estimation.SlopeCoefficients;
import org.scaleborn.linereg.estimation.SlopeCoefficients.DefaultSlopeCoefficients;

/**
 * Solves the coefficient derivation equation using math-commons library and the Cholesky
 * decomposition due to the Created by mbok on 18.03.17.
 */
public class CommonsMathSolver implements DerivationEquationSolver {

  @Override
  public SlopeCoefficients estimateCoefficients(final DerivationEquation eq)
      throws EstimationException {
    final double[][] sourceTriangleMatrix = eq.getCovarianceLowerTriangularMatrix();
    // Copy matrix and enhance it to a full matrix as expected by CholeskyDecomposition
    // FIXME: Avoid copy job to speed-up the solving process e.g. by extending the CholeskyDecomposition constructor
    final int length = sourceTriangleMatrix.length;
    final double[][] matrix = new double[length][];
    for (int i = 0; i < length; i++) {
      matrix[i] = new double[length];
      final double[] s = sourceTriangleMatrix[i];
      final double[] t = matrix[i];
      for (int j = 0; j <= i; j++) {
        t[j] = s[j];
      }
      for (int j = i + 1; j < length; j++) {
        t[j] = sourceTriangleMatrix[j][i];
      }
    }
    final RealMatrix coefficients =
        new Array2DRowRealMatrix(matrix, false);
    try {
      final DecompositionSolver solver = new CholeskyDecomposition(coefficients).getSolver();
      final RealVector constants = new ArrayRealVector(eq.getConstraints(), true);
      final RealVector solution = solver.solve(constants);
      return new DefaultSlopeCoefficients(solution.toArray());
    } catch (final NonPositiveDefiniteMatrixException e) {
      throw new EstimationException("Matrix inversion error due to data is linearly dependent", e);
    }
  }
}
