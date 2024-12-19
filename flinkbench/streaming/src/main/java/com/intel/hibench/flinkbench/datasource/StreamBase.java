/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intel.hibench.flinkbench.datasource;

import com.intel.hibench.flinkbench.util.KeyedTupleSchema;
import com.intel.hibench.flinkbench.util.FlinkBenchConfig;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public abstract class StreamBase {


  private SourceFunction<Tuple2<String, String>> dataStream;
  private RichSinkFunction<String> sinkFunction;

  public SourceFunction<Tuple2<String, String>> getDataStream() {
    return this.dataStream;
  }

  public RichSinkFunction<String> getSinkFunction() {
    return this.sinkFunction;
  }

  public void createDataStream(FlinkBenchConfig config) {

    Properties consumer_properties = new Properties();
    consumer_properties.setProperty("group.id", config.consumerGroup);
    consumer_properties.setProperty("bootstrap.servers", config.brokerList);
    consumer_properties.setProperty("auto.offset.reset", config.offsetReset);

    // kerberos security -- ldh 2024-10-26 00:10:22
    try {
      String currentDir = System.getProperty("user.dir");
      Properties security_props = new Properties();
      security_props.load(new FileInputStream(currentDir+ File.separator+"/conf/security.properties"));
      if (Boolean.parseBoolean(security_props.getProperty("iskerberos")))
      {
        consumer_properties.setProperty("sasl.kerberos.service.name","kafka");
        consumer_properties.setProperty("sasl.mechanism", "GSSAPI");
        consumer_properties.setProperty("security.protocol", "SASL_PLAINTEXT");
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    this.dataStream= new FlinkKafkaConsumer<>(
            config.topic,
            new KeyedTupleSchema(),
            consumer_properties
    );
  }

  public void createSinkFunction(FlinkBenchConfig config) {

    Properties producer_properties = new Properties();
    producer_properties.setProperty("bootstrap.servers", config.brokerList);

    // kerberos security -- ldh 2024-10-26 00:10:22
    try {
      String currentDir = System.getProperty("user.dir");
      Properties security_props = new Properties();
      security_props.load(new FileInputStream(currentDir+ File.separator+"/conf/security.properties"));
      if (Boolean.parseBoolean(security_props.getProperty("iskerberos")))
      {
        producer_properties.setProperty("sasl.kerberos.service.name","kafka");
        producer_properties.setProperty("sasl.mechanism", "GSSAPI");
        producer_properties.setProperty("security.protocol", "SASL_PLAINTEXT");
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    this.sinkFunction = new FlinkKafkaProducer<>(
            config.reportTopic,
//            "aaa",
            new SimpleStringSchema(),
            producer_properties
    );
  }

  public void processStream(FlinkBenchConfig config) throws Exception {
  }
}
