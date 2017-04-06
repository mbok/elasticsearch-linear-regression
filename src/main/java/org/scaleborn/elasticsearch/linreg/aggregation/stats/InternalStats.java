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

import static java.util.Collections.emptyMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.search.aggregations.InternalAggregation;
import org.elasticsearch.search.aggregations.pipeline.PipelineAggregator;
import org.scaleborn.elasticsearch.linreg.aggregation.support.StateInputStreamAdapter;
import org.scaleborn.elasticsearch.linreg.aggregation.support.StateOutputStreamAdapter;
import org.scaleborn.linereg.evaluation.DerivationEquation;
import org.scaleborn.linereg.evaluation.DerivationEquationBuilder;
import org.scaleborn.linereg.evaluation.SlopeCoefficients;
import org.scaleborn.linereg.evaluation.commons.CommonsMathSolver;
import org.scaleborn.linereg.statistics.Statistics;
import org.scaleborn.linereg.statistics.StatsBuilder;
import org.scaleborn.linereg.statistics.StatsModel;
import org.scaleborn.linereg.statistics.StatsSampling;

/**
 * Created by mbok on 21.03.17.
 */
public class InternalStats extends InternalAggregation implements Stats {

  private static final Logger LOGGER = Loggers.getLogger(InternalStats.class);
  /**
   * per shard sampling needed to compute stats
   */
  private StatsSampling<?> sampling;
  /**
   * final result
   */
  private Statistics results;

  /**
   * Features count
   */
  private int featuresCount;

  /**
   * per shard ctor
   */
  protected InternalStats(String name, int featuresCount, StatsSampling<?> linRegSampling,
      Statistics results,
      List<PipelineAggregator> pipelineAggregators, Map<String, Object> metaData) {
    super(name, pipelineAggregators, metaData);
    this.featuresCount = featuresCount;
    this.sampling = linRegSampling;
    this.results = results;
  }

  /**
   * Read from a stream.
   */
  public InternalStats(StreamInput in) throws IOException {
    super(in);
    this.featuresCount = in.readInt();
    if (in.readBoolean()) {
      this.sampling = StatsAggregationBuilder.buildSampling(this.featuresCount);
      StateInputStreamAdapter streamAdapter = new StateInputStreamAdapter(in);
      this.sampling.loadState(streamAdapter);
    }
    if (in.readBoolean()) {
      this.results = new DefaultStatistics(in.readDouble(), in.readDouble());
    }
  }

  @Override
  protected void doWriteTo(StreamOutput out) throws IOException {
    out.writeInt(this.featuresCount);
    out.writeBoolean(this.sampling != null);
    if (this.sampling != null) {
      StateOutputStreamAdapter outAdapter = new StateOutputStreamAdapter(out);
      this.sampling.saveState(outAdapter);
    }
    out.writeBoolean(this.results != null);
    if (this.results != null) {
      out.writeDouble(this.results.getRss());
      out.writeDouble(this.results.getMse());
    }
  }

  @Override
  public String getWriteableName() {
    return StatsAggregationBuilder.NAME;
  }

  static class Fields {

    public static final String RSS = "rss";
    public static final String MSE = "mse";
  }

  @Override
  public double getRss() {
    if (results == null) {
      return Double.NaN;
    }
    return results.getRss();
  }

  @Override
  public double getMse() {
    if (results == null) {
      return Double.NaN;
    }
    return results.getMse();
  }

  @Override
  public XContentBuilder doXContentBody(XContentBuilder builder, Params params) throws IOException {
    if (results != null) {
      // RSS
      builder.field(Fields.RSS, results.getRss());
      // MSE
      builder.field(Fields.MSE, results.getMse());
    }
    return builder;
  }

  @Override
  public Object getProperty(List<String> path) {
    if (path.isEmpty()) {
      return this;
    } else if (path.size() == 1) {
      String element = path.get(0);
      if (results == null) {
        return emptyMap();
      }
      switch (element) {
        case "rss":
          return results.getRss();
        case "mse":
          return results.getMse();
        default:
          throw new IllegalArgumentException(
              "Found unknown path element [" + element + "] in [" + getName() + "]");
      }
    } else {
      throw new IllegalArgumentException("path not supported for [" + getName() + "]: " + path);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public InternalAggregation doReduce(List<InternalAggregation> aggregations,
      ReduceContext reduceContext) {
    // merge samples across all shards
    List<InternalAggregation> aggs = new ArrayList<>(aggregations);
    aggs.removeIf(p -> ((InternalStats) p).sampling == null);

    // return empty result iff all samples are null
    if (aggs.isEmpty()) {
      return new InternalStats(name, featuresCount, null, new DefaultStatistics(0, 0),
          pipelineAggregators(),
          getMetaData());
    }

    StatsSampling composedSampling = StatsAggregationBuilder
        .buildSampling(featuresCount);
    for (int i = 0; i < aggs.size(); ++i) {
      LOGGER.info("Merging sampling={}: {}", i, ((InternalStats) aggs.get(i)).sampling);
      composedSampling.merge(((InternalStats) aggs.get(i)).sampling);
    }

    // Linear regression evaluation
    DerivationEquation derivationEquation = new DerivationEquationBuilder()
        .buildDerivationEquation(composedSampling);
    CommonsMathSolver commonsMathSolver = new CommonsMathSolver();
    SlopeCoefficients slopeCoefficients = commonsMathSolver.solveCoefficients(derivationEquation);
    StatsBuilder statsBuilder = new StatsBuilder();
    Statistics statsResult = statsBuilder
        .buildStats(new StatsModel(composedSampling, slopeCoefficients));
    LOGGER.info("Evaluated linear with {} and stats {}", slopeCoefficients, statsResult);
    return new InternalStats(name, featuresCount, composedSampling, statsResult,
        pipelineAggregators(), getMetaData());
  }
}
