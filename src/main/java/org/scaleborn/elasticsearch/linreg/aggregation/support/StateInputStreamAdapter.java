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

import java.io.IOException;
import org.elasticsearch.common.io.stream.StreamInput;
import org.scaleborn.linereg.sampling.io.StateInputStream;

/**
 * Adaptor for {@link org.scaleborn.linereg.sampling.io.StateInputStream}
 * behind of {@link org.elasticsearch.common.io.stream.StreamInput}.
 * Created by mbok on 30.03.17.
 */
public class StateInputStreamAdapter implements StateInputStream {

  private StreamInput streamInput;

  public StateInputStreamAdapter(final StreamInput streamInput) {
    this.streamInput = streamInput;
  }

  @Override
  public int readInt() throws IOException {
    return streamInput.readInt();
  }

  @Override
  public long readLong() throws IOException {
    return streamInput.readLong();
  }

  @Override
  public double readDouble() throws IOException {
    return streamInput.readDouble();
  }

  @Override
  public double[] readDoubleArray() throws IOException {
    return streamInput.readDoubleArray();
  }

  @Override
  public double[][] readDoubleMatrix() throws IOException {
    int size = streamInput.readInt();
    double[][] matrix = new double[size][];
    for (int i = 0; i < size; i++) {
      matrix[i] = streamInput.readDoubleArray();
    }
    return matrix;
  }

  @Override
  public boolean readBoolean() throws IOException {
    return streamInput.readBoolean();
  }
}
