package org.scaleborn.elasticsearch.linreg.aggregation.predict;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.apache.lucene.index.LeafReaderContext;
import org.elasticsearch.search.DocValueFormat;
import org.elasticsearch.search.aggregations.Aggregator;
import org.elasticsearch.search.aggregations.InternalAggregation;
import org.elasticsearch.search.aggregations.LeafBucketCollector;
import org.elasticsearch.search.aggregations.metrics.NumericMetricsAggregator;
import org.elasticsearch.search.aggregations.pipeline.PipelineAggregator;
import org.elasticsearch.search.aggregations.support.ValuesSource;
import org.elasticsearch.search.aggregations.support.ValuesSource.Numeric;
import org.elasticsearch.search.internal.SearchContext;

/**
 * Aggregator for prediction aggregation.
 * Created by mbok on 10.03.17.
 */
public class PredictionAggregator extends NumericMetricsAggregator.MultiValue {

  private final Numeric valuesSource;

  public PredictionAggregator(String name, ValuesSource.Numeric valuesSource, DocValueFormat format,
      SearchContext context,
      Aggregator parent, List<PipelineAggregator> pipelineAggregators,
      Map<String, Object> metaData) throws IOException {
    super(name, context, parent, pipelineAggregators, metaData);
    this.valuesSource = valuesSource;
  }

  @Override
  public boolean hasMetric(String name) {
    return false;
  }

  @Override
  public double metric(String name, long owningBucketOrd) {
    return 0;
  }

  @Override
  protected LeafBucketCollector getLeafCollector(LeafReaderContext ctx, LeafBucketCollector sub)
      throws IOException {
    return null;
  }

  @Override
  public InternalAggregation buildAggregation(long bucket) throws IOException {
    return null;
  }

  @Override
  public InternalAggregation buildEmptyAggregation() {
    return null;
  }
}
