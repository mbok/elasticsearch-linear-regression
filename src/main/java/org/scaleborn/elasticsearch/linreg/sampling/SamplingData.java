package org.scaleborn.elasticsearch.linreg.sampling;

/**
 * SamplingData data over the result set for building the linear model.
 * Created by mbok on 12.03.17.
 */
public abstract class SamplingData {

  private long count = 0;
  private double[] sums;

  public SamplingData(int inputCount) {
    sums = new double[inputCount];
  }

  public void sample(double[] inputs) {
    count++;
    for (int i = 0; i < inputs.length; i++) {
      sums[i] += inputs[i];
    }
    doSample(inputs);
  }

  protected abstract void doSample(double[] inputs);

  public void merge(SamplingData from, SamplingData to) {
    to.count += from.count;
    for (int i = 0; i < from.sums.length; i++) {
      to.sums[i] += from.sums[i];
    }
    doMerge(from, to);
  }

  public abstract void doMerge(SamplingData from, SamplingData to);
}
