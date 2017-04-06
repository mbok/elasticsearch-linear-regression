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
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.index.LeafReaderContext;
import org.elasticsearch.index.fielddata.NumericDoubleValues;
import org.elasticsearch.search.MultiValueMode;
import org.elasticsearch.search.aggregations.support.ValuesSource.Numeric;

/**
 * Class to encapsulate a set of ValuesSource objects labeled by field name
 */
public abstract class MultiValuesSource<VS extends ValuesSource> {

  protected MultiValueMode multiValueMode;
  protected String[] names;
  protected VS[] values;

  public static class NumericMultiValuesSource extends MultiValuesSource<ValuesSource.Numeric> {

    public NumericMultiValuesSource(List<NamedValuesSourceSpec<Numeric>> valuesSources,
        MultiValueMode multiValueMode) {
      super(valuesSources, multiValueMode, new ValuesSource.Numeric[0]);
    }

    public NumericDoubleValues getField(final int ordinal, LeafReaderContext ctx)
        throws IOException {
      if (ordinal > names.length) {
        throw new IndexOutOfBoundsException(
            "ValuesSource array index " + ordinal + " out of bounds");
      }
      return multiValueMode.select(values[ordinal].doubleValues(ctx), Double.NEGATIVE_INFINITY);
    }
  }

  public static class BytesMultiValuesSource extends MultiValuesSource<ValuesSource.Bytes> {

    public BytesMultiValuesSource(List<NamedValuesSourceSpec<ValuesSource.Bytes>> valuesSources,
        MultiValueMode multiValueMode) {
      super(valuesSources, multiValueMode, new ValuesSource.Bytes[0]);
    }

    public Object getField(final int ordinal, LeafReaderContext ctx) throws IOException {
      return values[ordinal].bytesValues(ctx);
    }
  }

  public static class GeoPointValuesSource extends MultiValuesSource<ValuesSource.GeoPoint> {

    public GeoPointValuesSource(List<NamedValuesSourceSpec<ValuesSource.GeoPoint>> valuesSources,
        MultiValueMode multiValueMode) {
      super(valuesSources, multiValueMode, new ValuesSource.GeoPoint[0]);
    }
  }

  private MultiValuesSource(List<? extends NamedValuesSourceSpec<VS>> valuesSources,
      MultiValueMode multiValueMode, VS[] emptyArray) {
    if (valuesSources != null) {
      this.names = new String[valuesSources.size()];
      List<VS> valuesList = new ArrayList<VS>(valuesSources.size());
      int i = 0;
      for (NamedValuesSourceSpec<VS> spec : valuesSources) {
        this.names[i++] = spec.getName();
        valuesList.add(spec.getValuesSource());
      }
      this.values = valuesList.toArray(emptyArray);
    } else {
      this.names = new String[0];
      this.values = emptyArray;
    }
    this.multiValueMode = multiValueMode;
  }

  public boolean needsScores() {
    boolean needsScores = false;
    for (ValuesSource value : values) {
      needsScores |= value.needsScores();
    }
    return needsScores;
  }

  public String[] fieldNames() {
    return this.names;
  }
}