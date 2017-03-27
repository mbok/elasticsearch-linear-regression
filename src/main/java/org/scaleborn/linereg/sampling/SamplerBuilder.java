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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Builder to composite multiple samplers into one.
 * Created by mbok on 27.03.17.
 */
public class SamplerBuilder {

  private static class CompoundSampler implements Sampler<CompoundSampler> {

    private List<? extends Sampler<?>> samplers;

    private CompoundSampler() {
      samplers = new ArrayList<>();
    }

    private CompoundSampler(final List<? extends Sampler<?>> samplers) {
      this.samplers = samplers;
    }

    @Override
    public void sample(final double[] featureValues, final double responseValue) {
      for (Sampler<?> sampler : samplers) {
        sampler.sample(featureValues, responseValue);
      }
    }

    @Override
    public void merge(final CompoundSampler fromSample) {
      for (int i = 0; i < samplers.size(); i++) {
        Sampler from = fromSample.samplers.get(i);
        Sampler to = samplers.get(i);
        to.merge(from);
      }
    }
  }

  private List<? extends Sampler<?>> samplers;


  public SamplerBuilder(final List<? extends Sampler<?>> samplers) {
    this.samplers = samplers;
  }

  public static SamplerBuilder forContext(SamplingContext<?> context) {
    return new SamplerBuilder(Collections.singletonList(context));
  }

  public SamplerBuilder addSampler(Sampler<?> sampler) {
    List<Sampler<?>> copySamplers = new ArrayList<>(samplers);
    copySamplers.add(sampler);
    return new SamplerBuilder(copySamplers);
  }

  public Sampler<?> build() {
    return new CompoundSampler(samplers);
  }
}
