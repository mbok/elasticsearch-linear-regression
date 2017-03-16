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

package org.scaleborn.elasticsearch.linreg;

import java.util.Arrays;
import java.util.List;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.plugins.SearchPlugin;
import org.scaleborn.elasticsearch.linreg.aggregation.predict.InternalPrediction;
import org.scaleborn.elasticsearch.linreg.aggregation.predict.PredictAggregationBuilder;

public class LinearRegressionPlugin extends Plugin implements SearchPlugin {

  @Override
  public List<AggregationSpec> getAggregations() {
    AggregationSpec predict = new AggregationSpec(PredictAggregationBuilder.NAME,
        PredictAggregationBuilder::new, PredictAggregationBuilder::parse)
        .addResultReader(InternalPrediction::new);
    return Arrays.asList(predict);
  }

  @Override
  public List<PipelineAggregationSpec> getPipelineAggregations() {
    // TODO Auto-generated method stub
    return SearchPlugin.super.getPipelineAggregations();
  }

}
