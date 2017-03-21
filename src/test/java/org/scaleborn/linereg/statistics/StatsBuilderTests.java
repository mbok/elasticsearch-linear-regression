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

package org.scaleborn.linereg.statistics;

import org.elasticsearch.test.ESTestCase;
import org.junit.Test;
import org.scaleborn.linereg.Model;
import org.scaleborn.linereg.TestModels;
import org.scaleborn.linereg.TestModels.TestModel;
import org.scaleborn.linereg.sampling.SampledData;

/**
 * Tests for {@link StatsBuilder}.
 * Created by mbok on 19.03.17.
 */
public class StatsBuilderTests extends ESTestCase {

  @Test
  public void testStats() {
    testStatsForModel(TestModels.SIMPLE_MODEL_1);
    testStatsForModel(TestModels.MULTI_FEATURES_2_MODEL_1);
    testStatsForModel(TestModels.MULTI_FEATURES_3_MODEL_1);
  }

  private void testStatsForModel(final TestModel testModel) {
    Model<SampledData> linearModel = testModel.evaluateModel();
    Statistics statistics = new StatsBuilder().buildStats(linearModel);
    testModel.assertStatistics(statistics);
  }
}
