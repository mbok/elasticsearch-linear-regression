/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.elasticsearch.search.aggregations.support;

import java.io.IOException;
import java.util.Map;
import org.apache.lucene.index.LeafReaderContext;
import org.elasticsearch.index.fielddata.NumericDoubleValues;
import org.elasticsearch.search.MultiValueMode;

/**
 * Class to encapsulate a set of ValuesSource objects labeled by field name
 */
public abstract class MultiValuesSource<VS extends ValuesSource> {

  protected MultiValueMode multiValueMode;
  protected String[] names;
  protected VS[] values;

  private MultiValuesSource(final Map<String, ?> valuesSources,
      final MultiValueMode multiValueMode) {
    if (valuesSources != null) {
      this.names = valuesSources.keySet().toArray(new String[0]);
    }
    this.multiValueMode = multiValueMode;
  }

  public boolean needsScores() {
    boolean needsScores = false;
    for (final ValuesSource value : this.values) {
      needsScores |= value.needsScores();
    }
    return needsScores;
  }

  public String[] fieldNames() {
    return this.names;
  }

  public static class NumericMultiValuesSource extends MultiValuesSource<ValuesSource.Numeric> {

    public NumericMultiValuesSource(final Map<String, ValuesSource.Numeric> valuesSources,
        final MultiValueMode multiValueMode) {
      super(valuesSources, multiValueMode);
      if (valuesSources != null) {
        this.values = valuesSources.values().toArray(new ValuesSource.Numeric[0]);
      } else {
        this.values = new ValuesSource.Numeric[0];
      }
    }

    public NumericDoubleValues getField(final int ordinal, final LeafReaderContext ctx)
        throws IOException {
      if (ordinal > this.names.length) {
        throw new IndexOutOfBoundsException(
            "ValuesSource array index " + ordinal + " out of bounds");
      }
      return this.multiValueMode
          .select(this.values[ordinal].doubleValues(ctx), Double.NEGATIVE_INFINITY);
    }
  }

  public static class BytesMultiValuesSource extends MultiValuesSource<ValuesSource.Bytes> {

    public BytesMultiValuesSource(final Map<String, ValuesSource.Bytes> valuesSources,
        final MultiValueMode multiValueMode) {
      super(valuesSources, multiValueMode);
      this.values = valuesSources.values().toArray(new ValuesSource.Bytes[0]);
    }

    public Object getField(final int ordinal, final LeafReaderContext ctx) throws IOException {
      return this.values[ordinal].bytesValues(ctx);
    }
  }

  public static class GeoPointValuesSource extends MultiValuesSource<ValuesSource.GeoPoint> {

    public GeoPointValuesSource(final Map<String, ValuesSource.GeoPoint> valuesSources,
        final MultiValueMode multiValueMode) {
      super(valuesSources, multiValueMode);
      this.values = valuesSources.values().toArray(new ValuesSource.GeoPoint[0]);
    }
  }
}