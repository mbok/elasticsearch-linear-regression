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

package org.scaleborn.linereg.calculation.statistics;

/**
 * Created by mbok on 19.03.17.
 */
public interface Statistics {

  /**
   * @return residual sum of squares (RSS), also known as the sum of squared residuals (SSR) or the
   * sum of squared errors of prediction (SSE), of the evaluated linear model. It is a measure of
   * the discrepancy between the data and the linear estimation model.
   */
  double getRss();

  /**
   * @return the mean squared error (MSE) of the evaluated linear model. Is's the average RSS.
   */
  double getMse();

  /**
   * @return R², coefficient of determination
   */
  double getR2();

  /**
   * Default statistics bean.
   */
  public class DefaultStatistics implements Statistics {

    private final double rss;
    private final double mse;
    private final double r2;

    public DefaultStatistics(final double rss, final double mse, final double r2) {
      this.rss = rss;
      this.mse = mse;
      this.r2 = r2;
    }

    @Override
    public double getRss() {
      return this.rss;
    }

    @Override
    public double getMse() {
      return this.mse;
    }

    @Override
    public double getR2() {
      return this.r2;
    }

    @Override
    public String toString() {
      return "DefaultStatistics{" +
          "rss=" + this.rss +
          ", mse=" + this.mse +
          ", r2=" + this.r2 +
          '}';
    }
  }
}
