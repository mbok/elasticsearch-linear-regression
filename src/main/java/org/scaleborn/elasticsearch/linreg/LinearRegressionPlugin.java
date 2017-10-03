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

import java.util.ArrayList;
import java.util.List;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.plugins.SearchPlugin;
import org.scaleborn.elasticsearch.linreg.aggregation.predict.InternalPrediction;
import org.scaleborn.elasticsearch.linreg.aggregation.predict.PredictionAggregationBuilder;
import org.scaleborn.elasticsearch.linreg.aggregation.predict.PredictionAggregationParser;
import org.scaleborn.elasticsearch.linreg.aggregation.stats.InternalStats;
import org.scaleborn.elasticsearch.linreg.aggregation.stats.StatsAggregationBuilder;
import org.scaleborn.elasticsearch.linreg.aggregation.stats.StatsAggregationParser;
import org.scaleborn.elasticsearch.linreg.functionscore.LinearRegressionErrorScoreFunctionBuilder;

/**
 * Plugin definition for linear regression aggregations.
 */
public class LinearRegressionPlugin extends Plugin implements SearchPlugin {

  @Override
  public List<AggregationSpec> getAggregations() {
    final List<AggregationSpec> aggregations = new ArrayList<>();
    aggregations.add(new AggregationSpec(StatsAggregationBuilder.NAME, StatsAggregationBuilder::new,
        new StatsAggregationParser()).addResultReader(InternalStats::new));
    aggregations.add(
        new AggregationSpec(PredictionAggregationBuilder.NAME, PredictionAggregationBuilder::new,
            new PredictionAggregationParser()).addResultReader(InternalPrediction::new));
    return aggregations;
  }

  @Override
  public List<ScoreFunctionSpec<?>> getScoreFunctions() {
    final List<ScoreFunctionSpec<?>> functionSpecs = new ArrayList<>();
    functionSpecs.add(new ScoreFunctionSpec<>(LinearRegressionErrorScoreFunctionBuilder.NAME,
        LinearRegressionErrorScoreFunctionBuilder::new,
        LinearRegressionErrorScoreFunctionBuilder::fromXContent));
    return functionSpecs;
  }
}
