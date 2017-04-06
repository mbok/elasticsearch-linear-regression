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

package org.elasticsearch.search.aggregations.support;

/**
 * Wrapper for values source config referenced by a name.
 * Created by mbok on 03.04.17.
 */
public class NamedValuesSourceConfigSpec<VS extends ValuesSource> {

  private String name;

  private ValuesSourceConfig<VS> config;

  public NamedValuesSourceConfigSpec(final String name,
      final ValuesSourceConfig<VS> config) {
    this.name = name;
    this.config = config;
  }

  public String getName() {
    return name;
  }

  public ValuesSourceConfig<VS> getConfig() {
    return config;
  }
}
