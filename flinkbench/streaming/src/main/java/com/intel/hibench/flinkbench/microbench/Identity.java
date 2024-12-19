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

package com.intel.hibench.flinkbench.microbench;

import com.intel.hibench.flinkbench.datasource.StreamBase;
import com.intel.hibench.flinkbench.util.FlinkBenchConfig;
import com.intel.hibench.flinkbench.util.KeyedTupleSchema;
import com.intel.hibench.flinkbench.util.StringTupleSchema;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collections;
import java.util.Properties;


public class Identity extends StreamBase {

    Logger log = LogManager.getLogger(Identity.class);
    @Override
    public void processStream(final FlinkBenchConfig config) throws Exception {

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setBufferTimeout(config.bufferTimeout);
//        设置检查点
//        env.enableCheckpointing(10000);
//        env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
//        env.getCheckpointConfig().setCheckpointStorage("hdfs://nameservice1/flinkcheckpoint");
//        env.getCheckpointConfig().setCheckpointTimeout(50000);

        createDataStream(config);
        createSinkFunction(config);
        DataStream<Tuple2<String, String>> dataStream = env.addSource(getDataStream());
        dataStream.map(new MapFunction<Tuple2<String, String>, String>() {
            @Override
            public String map(Tuple2<String, String> value) {
                return value.f0+":"+System.currentTimeMillis();
            }
        }).disableChaining().addSink(getSinkFunction()).disableChaining();

        env.execute("Identity Job");
    }

//    public static void main(String[] args) throws Exception {
//        System.setProperty("java.security.auth.login.config", "/Users/ldh/Downloads/jaas.conf");
//        System.setProperty("java.security.krb5.conf", "/Users/ldh/Downloads/krb5.conf");
//        FlinkBenchConfig config = new FlinkBenchConfig();
//        config.brokerList="tdh01:9092, tdh02:9092, tdh03:9092";
//        config.topic="identity";
//        config.reportTopic="aaa";
//        config.consumerGroup="HiBench";
//        config.offsetReset="latest";
//        System.out.println(System.getProperty("user.dir"));
//        Identity identity = new Identity();
//        identity.processStream(config);
//    }
}
