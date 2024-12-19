package com.intel.hibench.flinkbench.util;

import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.typeutils.TupleTypeInfo;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.apache.flink.streaming.util.serialization.KeyedDeserializationSchema;
import org.apache.flink.streaming.util.serialization.KeyedSerializationSchema;
import org.apache.flink.util.Collector;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.io.IOException;

public class KeyedTupleSchema implements KeyedSerializationSchema<Tuple2<String, String>>, KeyedDeserializationSchema<Tuple2<String, String>> {

  @Override
  public byte[] serializeKey(Tuple2<String, String> element) {
    return element.f0.getBytes();
  }

  @Override
  public byte[] serializeValue(Tuple2<String, String> element) {
    return element.f1.getBytes();
  }

  @Override
  public Tuple2<String, String> deserialize(byte[] messageKey, byte[] message, String topic, int partition, long offset) throws IOException {
    return new Tuple2<String, String>(new String(messageKey), new String(message));
  }

  @Override
  public boolean isEndOfStream(Tuple2<String, String> nextElement) {
    return false;
  }

  @Override
  public TypeInformation<Tuple2<String, String>> getProducedType() {
    return new TupleTypeInfo<Tuple2<String, String>>(TypeExtractor.createTypeInfo(String.class), TypeExtractor.createTypeInfo(String.class));
  }

  // 以下为添加
  @Override
  public String getTargetTopic(Tuple2<String, String> stringStringTuple2) {
    System.out.println("my getTargetTopic");
    return "";
  }

  @Override
  public Tuple2<String, String> deserialize(ConsumerRecord<byte[], byte[]> record) throws IOException {
    System.out.println("my deserialize");
    return null;
  }

  @Override
  public void open(DeserializationSchema.InitializationContext context) throws Exception {
    KeyedDeserializationSchema.super.open(context);
  }

  @Override
  public void deserialize(ConsumerRecord<byte[], byte[]> message, Collector<Tuple2<String, String>> out) throws Exception {
    out.collect(new Tuple2<>(new String(message.key()), new String(message.value())));
  }
}

