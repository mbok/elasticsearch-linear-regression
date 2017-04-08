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

import org.scaleborn.linereg.sampling.Sampling.CoefficientLinearTermSampling;
import org.scaleborn.linereg.sampling.Sampling.CoefficientSquareTermSampling;
import org.scaleborn.linereg.sampling.Sampling.InterceptSampling;
import org.scaleborn.linereg.sampling.Sampling.ResponseVarianceTermSampling;
import org.scaleborn.linereg.sampling.Sampling.SamplingContext;

/**
 * Created by mbok on 26.03.17.
 */
public interface ModelSamplingFactory<C extends SamplingContext<C>> {

  C createContext(int featuresCount);

  ResponseVarianceTermSampling<?> createResponseVarianceTermSampling(C context);

  CoefficientLinearTermSampling<?> createCoefficientLinearTermSampling(C context);

  CoefficientSquareTermSampling<?> createCoefficientSquareTermSampling(C context);

  InterceptSampling<?> createInterceptSampling(C context);
}
