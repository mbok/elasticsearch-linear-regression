package org.scaleborn.elasticsearch.linreg.sampling.exact;

import org.scaleborn.elasticsearch.linreg.sampling.SamplingData;

/**
 * Sampling data for a linear model solved exactly. Doing this that way has a
 * time complexity of O(C * C * N) and a memory consumption of O(C * C), where
 * C is the count of variables and N the count of observations / documents.
 * Created by mbok on 12.03.17.
 */
public class ExactSamplingData extends SamplingData {

  public ExactSamplingData(int inputCount) {
    super(inputCount);
  }

  @Override
  protected void doSample(double[] inputs) {

  }

  @Override
  public void doMerge(SamplingData from, SamplingData to) {

  }
}
