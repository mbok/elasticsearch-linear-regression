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

package org.scaleborn.linereg.sampling;

/**
 * Util for working in tests with sampled data.
 * Created by mbok on 18.03.17.
 */
public class SamplingUtil {

  /**
   * Fills the given sampling data with observations from given matrix. The target value for each
   * observation is the last element in the row.
   *
   * @param sampling sampling to fill with observations
   * @param observations observation as matrix. Each row represents an observation. The columns the
   * feature values expect of the last column which is the target variable value.
   */
  public static void fillWithObservations(Sampling<?> sampling, double[][] observations) {
    for (int i = 0; i < observations.length; i++) {
      sampling.sample(observations[i], observations[i][observations[i].length - 1]);
    }
  }
}
