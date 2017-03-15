package org.scaleborn.elasticsearch.linreg.sampling;

/**
 * SamplingData data over the result set for building the linear model.
 * Created by mbok on 12.03.17.
 */
public abstract class SamplingData {

  private int featuresCount;
  private long count = 0;
  private double[] featureSums;
  private double[] featureSquareSums;
  private double[] featureTargetProductSum;
  private double targetSum = 0;
  private double targetSquareSum = 0;

  public SamplingData(int featuresCount) {
    this.featuresCount = featuresCount;
    featureSums = new double[featuresCount];
    featureSquareSums = new double[featuresCount];
    featureTargetProductSum = new double[featuresCount];
  }

  public void sample(double[] featureValues, double targetValue) {
    count++;
    for (int i = 0; i < featuresCount; i++) {
      double v = featureValues[i];
      featureSums[i] += v;
      featureSquareSums[i] += v * v;
      featureTargetProductSum[i] += v * targetValue;
    }
    targetSum += targetValue;
    targetSquareSum += targetValue * targetValue;
    doSample(featureValues, targetValue);
  }

  protected abstract void doSample(double[] featureValues, double targetValue);

  public void merge(SamplingData from, SamplingData to) {
    to.count += from.count;
    for (int i = 0; i < from.featuresCount; i++) {
      to.featureSums[i] += from.featureSums[i];
      to.featureSquareSums[i] += from.featureSquareSums[i];
      to.featureTargetProductSum[i] += from.featureTargetProductSum[i];
    }
    to.targetSum += from.targetSum;
    to.targetSquareSum += from.targetSquareSum;
    doMerge(from, to);
  }

  public abstract void doMerge(SamplingData from, SamplingData to);

  public abstract double[][] getCovarianceMatrix();

  public int getFeaturesCount() {
    return featuresCount;
  }

  public long getCount() {
    return count;
  }

  public double[] getFeatureSums() {
    return featureSums;
  }

  public double[] getFeatureSquareSums() {
    return featureSquareSums;
  }

  public double[] getFeatureTargetProductSum() {
    return featureTargetProductSum;
  }

  public double getTargetSum() {
    return targetSum;
  }

  public double getTargetSquareSum() {
    return targetSquareSum;
  }
}
