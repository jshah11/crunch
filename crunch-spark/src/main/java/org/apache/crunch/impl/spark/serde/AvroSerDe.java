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
package org.apache.crunch.impl.spark.serde;

import com.google.common.base.Function;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.mapred.AvroWrapper;
import org.apache.avro.reflect.ReflectDatumReader;
import org.apache.avro.reflect.ReflectDatumWriter;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.crunch.types.avro.AvroType;
import org.apache.crunch.types.avro.Avros;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AvroSerDe<T> implements SerDe<T> {

  private AvroType<T> avroType;
  private transient DatumWriter<T> writer;
  private transient DatumReader<T> reader;

  public AvroSerDe(AvroType<T> avroType) {
    this.avroType = avroType;
    if (avroType.hasReflect() && avroType.hasSpecific()) {
      Avros.checkCombiningSpecificAndReflectionSchemas();
    }
  }

  private DatumWriter<T> getWriter() {
    if (writer == null) {
      if (avroType.hasReflect()) {
        writer = new ReflectDatumWriter<T>(avroType.getSchema());
      } else if (avroType.hasSpecific()) {
        writer = new SpecificDatumWriter<T>(avroType.getSchema());
      } else {
        writer = new GenericDatumWriter<T>(avroType.getSchema());
      }
    }
    return writer;
  }

  private DatumReader<T> getReader() {
    if (reader == null) {
      if (avroType.hasReflect()) {
        reader = new ReflectDatumReader<T>(avroType.getSchema());
      } else if (avroType.hasSpecific()) {
        reader = new SpecificDatumReader<T>(avroType.getSchema());
      } else {
        reader = new GenericDatumReader<T>(avroType.getSchema());
      }
    }
    return reader;
  }

  @Override
  public byte[] toBytes(T obj) throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Encoder encoder = EncoderFactory.get().binaryEncoder(out, null);
    getWriter().write(obj, encoder);
    encoder.flush();
    out.close();
    return out.toByteArray();
  }

  @Override
  public T fromBytes(byte[] bytes) {
    Decoder decoder = DecoderFactory.get().binaryDecoder(bytes, null);
    try {
      return getReader().read(null, decoder);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Function<byte[], T> fromBytesFunction() {
    return new Function<byte[], T>() {
      @Override
      public T apply(@Nullable byte[] input) {
        return fromBytes(input);
      }
    };
  }
}
