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
 * SamplingData data over the result set for building the linear model.
 * Created by mbok on 12.03.17.
 */
public abstract class SamplingData implements SampledData {

  protected int featuresCount;
  protected long count = 0;
  protected double[] featureSums;
  protected double[] featureTargetProductSums;
  protected double targetSum = 0;
  protected double targetSquareSum = 0;

  public SamplingData(int featuresCount) {
    this.featuresCount = featuresCount;
    featureSums = new double[featuresCount];
    featureTargetProductSums = new double[featuresCount];
  }

  public void sample(double[] featureValues, double targetValue) {
    count++;
    for (int i = 0; i < featuresCount; i++) {
      double v = featureValues[i];
      featureSums[i] += v;
      featureTargetProductSums[i] += v * targetValue;
    }
    targetSum += targetValue;
    targetSquareSum += targetValue * targetValue;
    doSample(featureValues, targetValue);
  }

  protected abstract void doSample(double[] featureValues, double targetValue);

  public void merge(SamplingData from) {
    count += from.count;
    for (int i = 0; i < featuresCount; i++) {
      featureSums[i] += from.featureSums[i];
      featureTargetProductSums[i] += from.featureTargetProductSums[i];
    }
    targetSum += from.targetSum;
    targetSquareSum += from.targetSquareSum;
    doMerge(from);
  }

  public abstract void doMerge(SamplingData from);

  @Override
  public abstract double[][] getCovarianceLowerTriangularMatrix();

  @Override
  public double[] getFeatureAverages() {
    double[] avgs = new double[featuresCount];
    if (count > 0) {
      for (int i = 0; i < featuresCount; i++) {
        avgs[i] = featureSums[i] / count;
      }
    }
    return avgs;
  }

  @Override
  public double getTargetAverage() {
    if (count > 0) {
      return targetSum / count;
    }
    return 0;
  }

  @Override
  public int getFeaturesCount() {
    return featuresCount;
  }

  @Override
  public long getCount() {
    return count;
  }

  @Override
  public double[] getFeatureTargetProductSums() {
    return featureTargetProductSums;
  }

  @Override
  public double getTargetSum() {
    return targetSum;
  }

  @Override
  public double getTargetSquareSum() {
    return targetSquareSum;
  }
}
