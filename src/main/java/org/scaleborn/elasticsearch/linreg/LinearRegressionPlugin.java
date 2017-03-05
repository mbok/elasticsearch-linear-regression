package org.scaleborn.elasticsearch.linreg;

import java.util.Arrays;
import java.util.List;

import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.plugins.SearchPlugin;
import org.elasticsearch.search.aggregations.metrics.stats.InternalStats;
import org.elasticsearch.search.aggregations.metrics.stats.StatsAggregationBuilder;

public class LinearRegressionPlugin extends Plugin implements SearchPlugin {

	@Override
	public List<AggregationSpec> getAggregations() {
		AggregationSpec predict = new AggregationSpec(StatsAggregationBuilder.NAME, StatsAggregationBuilder::new, StatsAggregationBuilder::parse)
        .addResultReader(InternalStats::new);
		return Arrays.asList(predict);
	}

	@Override
	public List<PipelineAggregationSpec> getPipelineAggregations() {
		// TODO Auto-generated method stub
		return SearchPlugin.super.getPipelineAggregations();
	}

}
