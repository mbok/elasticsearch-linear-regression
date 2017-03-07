package org.scaleborn.elasticsearch.linreg.aggregation.predict;

import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.QueryParseContext;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.AggregatorFactories;
import org.elasticsearch.search.aggregations.AggregatorFactory;
import org.elasticsearch.search.aggregations.InternalAggregation;
import org.elasticsearch.search.aggregations.bucket.filters.FiltersAggregationBuilder;
import org.elasticsearch.search.internal.SearchContext;

import java.io.IOException;

/**
 * Created by mbok on 06.03.17.
 */
public class PredictAggregationBuilder extends AbstractAggregationBuilder<PredictAggregationBuilder> {
    public static final String NAME = "linreg_predict";
    private static final InternalAggregation.Type TYPE = new InternalAggregation.Type(NAME);


    public PredictAggregationBuilder(StreamInput in) throws IOException {
        super(in, TYPE);
    }

    public static PredictAggregationBuilder parse(String aggregationName, QueryParseContext context)
            throws IOException {
        return null;
    }

    @Override
    protected void doWriteTo(StreamOutput out) throws IOException {

    }

    @Override
    protected AggregatorFactory<?> doBuild(SearchContext context, AggregatorFactory<?> parent, AggregatorFactories.Builder subfactoriesBuilder) throws IOException {
        return null;
    }

    @Override
    protected XContentBuilder internalXContent(XContentBuilder builder, Params params) throws IOException {
        return null;
    }

    @Override
    protected int doHashCode() {
        return 0;
    }

    @Override
    protected boolean doEquals(Object obj) {
        return false;
    }

    @Override
    public String getWriteableName() {
        return null;
    }
}
