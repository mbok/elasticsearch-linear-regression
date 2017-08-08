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

package org.scaleborn.elasticsearch.linreg.aggregation.stats;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.search.aggregations.pipeline.PipelineAggregator;
import org.scaleborn.elasticsearch.linreg.aggregation.support.BaseInternalAggregation;
import org.scaleborn.linereg.calculation.statistics.Statistics;
import org.scaleborn.linereg.calculation.statistics.StatsCalculator;
import org.scaleborn.linereg.calculation.statistics.StatsModel;
import org.scaleborn.linereg.estimation.SlopeCoefficients;

/**
 * Created by mbok on 21.03.17.
 */
public class InternalStats extends
    BaseInternalAggregation<StatsAggregationSampling, StatsResults, InternalStats> implements
    Stats {

  private static final Logger LOGGER = Loggers.getLogger(InternalStats.class);

  private static final StatsCalculator statsCalculator = new StatsCalculator();

  /**
   * per shard ctor
   */
  protected InternalStats(final String name, final int featuresCount,
      final StatsAggregationSampling linRegSampling,
      final StatsResults results,
      final List<PipelineAggregator> pipelineAggregators, final Map<String, Object> metaData) {
    super(name, featuresCount, linRegSampling, results, pipelineAggregators, metaData);
  }

  /**
   * Read from a stream.
   */
  public InternalStats(final StreamInput in) throws IOException {
    super(in, StatsResults::new);
  }

  @Override
  protected StatsAggregationSampling buildSampling(final int featuresCount) {
    return StatsAggregationBuilder.buildSampling(featuresCount);
  }


  @Override
  public String getWriteableName() {
    return StatsAggregationBuilder.NAME;
  }

  @Override
  public double getRss() {
    if (this.results == null) {
      return Double.NaN;
    }
    return this.results.statistics.getRss();
  }

  @Override
  public double getMse() {
    if (this.results == null) {
      return Double.NaN;
    }
    return this.results.statistics.getMse();
  }

  @Override
  public double getR2() {
    if (this.results == null) {
      return Double.NaN;
    }
    return this.results.statistics.getR2();
  }

  @Override
  public Object getDoProperty(final String element) {
    switch (element) {
      case "rss":
        return getRss();
      case "mse":
        return getMse();
      case "r2":
        return getR2();
    }
    return null;
  }

  @Override
  protected InternalStats buildInternalAggregation(final String name, final int featuresCount,
      final StatsAggregationSampling linRegSampling, final StatsResults results,
      final List<PipelineAggregator> pipelineAggregators, final Map<String, Object> metaData) {
    return new InternalStats(name, featuresCount, linRegSampling, results, pipelineAggregators,
        metaData);
  }

  @Override
  protected StatsResults buildResults(final StatsAggregationSampling composedSampling,
      final SlopeCoefficients slopeCoefficients, final double intercept) {
    final Statistics stats = statsCalculator
        .calculate(new StatsModel(composedSampling, slopeCoefficients));
    return new StatsResults(slopeCoefficients, intercept, stats);
  }

}
