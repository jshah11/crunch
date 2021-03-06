/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.crunch.io.avro.trevni;

import java.util.List;
import org.apache.avro.mapred.AvroJob;
import org.apache.crunch.impl.mr.run.RuntimeParameters;
import org.apache.crunch.io.FormatBundle;
import org.apache.crunch.io.ReadableSource;
import org.apache.crunch.ReadableData;
import org.apache.crunch.io.impl.FileSourceImpl;
import org.apache.crunch.types.avro.AvroType;
import org.apache.crunch.types.avro.Avros;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.trevni.avro.mapreduce.AvroTrevniKeyInputFormat;

import java.io.IOException;

public class TrevniKeySource<T> extends FileSourceImpl<T> implements ReadableSource<T> {

  private static <S> FormatBundle getBundle(AvroType<S> ptype) {
    return FormatBundle.forInput(AvroTrevniKeyInputFormat.class)
        .set(AvroJob.INPUT_IS_REFLECT, String.valueOf(ptype.hasReflect()))
        .set(AvroJob.INPUT_SCHEMA, ptype.getSchema().toString())
        .set(RuntimeParameters.DISABLE_COMBINE_FILE, Boolean.FALSE.toString())
        .set(Avros.REFLECT_DATA_FACTORY_CLASS, Avros.REFLECT_DATA_FACTORY.getClass().getName());
  }

  public TrevniKeySource(Path path, AvroType<T> ptype) {
    super(path, ptype, getBundle(ptype));
  }

  public TrevniKeySource(List<Path> paths, AvroType<T> ptype) {
    super(paths, ptype, getBundle(ptype));
  }

  @Override
  public String toString() {
    return "TrevniKey(" + pathsAsString() + ")";
  }

  @Override
  public Iterable<T> read(Configuration conf) throws IOException {
    return read(conf, new TrevniFileReaderFactory<T>((AvroType<T>) ptype));
  }

  @Override
  public ReadableData<T> asReadable() {
    return new TrevniReadableData<T>(paths, (AvroType<T>) ptype);
  }

}
