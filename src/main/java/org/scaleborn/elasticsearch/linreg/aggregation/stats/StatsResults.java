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
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.scaleborn.elasticsearch.linreg.aggregation.support.ModelResults;
import org.scaleborn.linereg.calculation.statistics.Statistics;
import org.scaleborn.linereg.calculation.statistics.Statistics.DefaultStatistics;
import org.scaleborn.linereg.evaluation.SlopeCoefficients;

/**
 * Created by mbok on 07.04.17.
 */
public class StatsResults extends ModelResults {

  static class Fields {

    public static final String RSS = "rss";
    public static final String MSE = "mse";
  }

  final Statistics statistics;

  public StatsResults(final SlopeCoefficients slopeCoefficients, final Statistics statistics) {
    super(slopeCoefficients);
    this.statistics = statistics;
  }

  public StatsResults(final StreamInput in) throws IOException {
    super(in);
    this.statistics = new DefaultStatistics(in.readDouble(), in.readDouble());
  }

  @Override
  public void writeTo(final StreamOutput out) throws IOException {
    super.writeTo(out);
    out.writeDouble(this.statistics.getRss());
    out.writeDouble(this.statistics.getMse());
  }

  @Override
  public XContentBuilder toXContent(final XContentBuilder builder, final Params params)
      throws IOException {
    // RSS
    builder.field(Fields.RSS, this.statistics.getRss());
    // MSE
    builder.field(Fields.MSE, this.statistics.getMse());
    return super.toXContent(builder, params);
  }

  @Override
  public String toString() {
    return "StatsResults{" +
        "statistics=" + this.statistics +
        "} " + super.toString();
  }
}
