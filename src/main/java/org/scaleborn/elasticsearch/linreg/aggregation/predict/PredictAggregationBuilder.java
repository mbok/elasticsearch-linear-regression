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

package org.scaleborn.elasticsearch.linreg.aggregation.predict;

import java.io.IOException;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.QueryParseContext;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.AggregatorFactories;
import org.elasticsearch.search.aggregations.AggregatorFactory;
import org.elasticsearch.search.aggregations.InternalAggregation;
import org.elasticsearch.search.internal.SearchContext;

/**
 * Created by mbok on 06.03.17.
 */
public class PredictAggregationBuilder extends
    AbstractAggregationBuilder<PredictAggregationBuilder> {

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
  protected AggregatorFactory<?> doBuild(SearchContext context, AggregatorFactory<?> parent,
      AggregatorFactories.Builder subfactoriesBuilder) throws IOException {
    return null;
  }

  @Override
  protected XContentBuilder internalXContent(XContentBuilder builder, Params params)
      throws IOException {
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
