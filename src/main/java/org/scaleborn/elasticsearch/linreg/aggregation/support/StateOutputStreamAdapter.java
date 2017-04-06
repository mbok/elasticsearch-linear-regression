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
import org.elasticsearch.common.io.stream.StreamOutput;
import org.scaleborn.linereg.sampling.io.StateOutputStream;

/**
 * Adapter for {@link StateOutputStream} backend by {@link StreamOutput}.
 * Created by mbok on 30.03.17.
 */
public class StateOutputStreamAdapter implements StateOutputStream {

  private StreamOutput streamOutput;

  public StateOutputStreamAdapter(final StreamOutput streamOutput) {
    this.streamOutput = streamOutput;
  }


  @Override
  public void writeInt(final int c) throws IOException {
    streamOutput.writeInt(c);
  }

  @Override
  public void writeLong(final long c) throws IOException {
    streamOutput.writeLong(c);
  }

  @Override
  public void writeDouble(final double c) throws IOException {
    streamOutput.writeDouble(c);
  }

  @Override
  public void writeDoubleArray(final double[] c) throws IOException {
    streamOutput.writeDoubleArray(c);
  }

  @Override
  public void writeDoubleMatrix(final double[][] c) throws IOException {
    streamOutput.writeInt(c.length);
    for (int i = 0; i < c.length; i++) {
      streamOutput.writeDoubleArray(c[i]);
    }
  }

  @Override
  public void writeBoolean(final boolean c) throws IOException {
    streamOutput.writeBoolean(c);
  }

}
