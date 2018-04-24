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

package org.scaleborn.elasticsearch.linreg.aggregation.support;

import static java.util.Collections.emptyMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.search.aggregations.InternalAggregation;
import org.elasticsearch.search.aggregations.pipeline.PipelineAggregator;
import org.scaleborn.linereg.calculation.intercept.InterceptCalculator;
import org.scaleborn.linereg.estimation.DerivationEquation;
import org.scaleborn.linereg.estimation.DerivationEquationBuilder;
import org.scaleborn.linereg.estimation.DerivationEquationSolver;
import org.scaleborn.linereg.estimation.DerivationEquationSolver.EstimationException;
import org.scaleborn.linereg.estimation.SlopeCoefficients;
import org.scaleborn.linereg.estimation.commons.CommonsMathSolver;

/**
 * Created by mbok on 07.04.17.
 */
public abstract class BaseInternalAggregation<S extends BaseSampling<S>, M extends ModelResults, A extends BaseInternalAggregation<S, M, A>> extends
    InternalAggregation {

  private static final Logger LOGGER = Loggers.getLogger(BaseInternalAggregation.class);

  private static final DerivationEquationBuilder derivationEquationBuilder = new DerivationEquationBuilder();
  private static final DerivationEquationSolver derivationEquationSolver = new CommonsMathSolver();
  private static final InterceptCalculator interceptCalculator = new InterceptCalculator();

  /**
   * per shard sampling needed to compute stats
   */
  private S sampling;

  /**
   * Features count
   */
  protected final int featuresCount;

  protected M results;

  protected BaseInternalAggregation(final String name, final int featuresCount, final S sampling,
      final M results,
      final List<PipelineAggregator> pipelineAggregators,
      final Map<String, Object> metaData) {
    super(name, pipelineAggregators, metaData);
    this.featuresCount = featuresCount;
    this.sampling = sampling;
    this.results = results;
  }

  protected BaseInternalAggregation(final StreamInput in, final Reader<M> resultsReader)
      throws IOException {
    super(in);
    this.featuresCount = in.readInt();
    if (in.readBoolean()) {
      this.sampling = buildSampling(this.featuresCount);
      final StateInputStreamAdapter streamAdapter = new StateInputStreamAdapter(in);
      this.sampling.loadState(streamAdapter);
    }
    if (in.readBoolean()) {
      this.results = resultsReader.read(in);
    }
  }

  protected abstract S buildSampling(int featuresCount);

  @Override
  protected void doWriteTo(final StreamOutput out) throws IOException {
    out.writeInt(this.featuresCount);
    out.writeBoolean(this.sampling != null);
    if (this.sampling != null) {
      final StateOutputStreamAdapter outAdapter = new StateOutputStreamAdapter(out);
      this.sampling.saveState(outAdapter);
    }
    out.writeBoolean(this.results != null);
    if (this.results != null) {
      this.results.writeTo(out);
    }
  }

  @Override
  public XContentBuilder doXContentBody(final XContentBuilder builder, final Params params)
      throws IOException {
    if (this.results != null) {
      return this.results.toXContent(builder, params);
    }
    return builder;
  }

  protected abstract Object getDoProperty(final String path);

  @Override
  public Object getProperty(final List<String> path) {
    if (path.isEmpty()) {
      return this;
    } else if (path.size() == 1) {
      final String element = path.get(0);
      if (this.results == null) {
        return emptyMap();
      }
      final Object prop = getDoProperty(element);
      if (prop != null) {
        return prop;
      } else {
        throw new IllegalArgumentException(
            "Found unknown path element [" + element + "] in [" + getName() + "]");
      }
    } else {
      throw new IllegalArgumentException("path not supported for [" + getName() + "]: " + path);
    }
  }


  @Override
  public InternalAggregation doReduce(final List<InternalAggregation> aggregations,
      final ReduceContext reduceContext) {
    // merge samples across all shards
    final List<InternalAggregation> aggs = new ArrayList<>(aggregations);
    aggs.removeIf(p -> ((BaseInternalAggregation) p).sampling == null);

    // return empty result if all samples are null
    if (aggs.isEmpty()) {
      return buildEmptyInternalAggregation();
    }

    final S composedSampling = buildSampling(this.featuresCount);
    for (int i = 0; i < aggs.size(); ++i) {
      LOGGER.debug("Merging sampling={}: {}", i, ((BaseInternalAggregation) aggs.get(i)).sampling);
      //noinspection unchecked
      composedSampling.merge((S) ((BaseInternalAggregation) aggs.get(i)).sampling);
    }

    if (composedSampling.getCount() <= composedSampling.getFeaturesCount()) {
      LOGGER.debug(
          "Insufficient amount of training data for model estimation, at least {} are required, given {}",
          composedSampling.getFeaturesCount() + 1, composedSampling.getCount());
      return buildEmptyInternalAggregation();
    }

    M evaluatedResults = null;
    try {
      evaluatedResults = evaluateResults(composedSampling);
    } catch (final EstimationException e) {
      LOGGER.debug(
          "Failed to estimate model", e);
      return buildEmptyInternalAggregation();
    }

    LOGGER.debug("Evaluated results: {}", evaluatedResults);
    return buildInternalAggregation(this.name, this.featuresCount, composedSampling,
        evaluatedResults,
        pipelineAggregators(), getMetaData());
  }

  private InternalAggregation buildEmptyInternalAggregation() {
    return buildInternalAggregation(this.name, this.featuresCount, null, null,
        pipelineAggregators(),
        getMetaData());
  }

  protected abstract A buildInternalAggregation(final String name, final int featuresCount,
      final S linRegSampling,
      final M results,
      final List<PipelineAggregator> pipelineAggregators, final Map<String, Object> metaData);

  protected abstract M buildResults(S composedSampling, SlopeCoefficients slopeCoefficients,
      double intercept);


  private M evaluateResults(final S composedSampling) throws EstimationException {
    // Linear regression estimation
    final DerivationEquation derivationEquation = derivationEquationBuilder
        .buildDerivationEquation(composedSampling);
    final SlopeCoefficients slopeCoefficients = derivationEquationSolver
        .estimateCoefficients(derivationEquation);
    final M buildResults = buildResults(composedSampling, slopeCoefficients,
        interceptCalculator.calculate(slopeCoefficients, composedSampling, composedSampling));
    return buildResults;
  }

  @Override
  protected int doHashCode() {
    return Objects.hash(this.sampling, this.results);
  }

  @Override
  protected boolean doEquals(final Object obj) {
    final BaseInternalAggregation other = (BaseInternalAggregation) obj;
    return Objects.equals(this.sampling, other.sampling) &&
        Objects.equals(this.results, other.results);
  }
}
