package org.scaleborn.elasticsearch.linreg.aggregation.predict;

/**
 * Prediction representation.
 * Created by mbok on 07.03.17.
 */
public interface Prediction {

  /**
   * @return the predicted value for the target variable
   */
  double getPrediction();
}
