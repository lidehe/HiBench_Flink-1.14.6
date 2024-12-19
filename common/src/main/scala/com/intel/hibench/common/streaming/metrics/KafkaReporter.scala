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
package com.intel.hibench.common.streaming.metrics

import java.util.Properties
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer

import java.io.{File, FileInputStream, FileNotFoundException, IOException}

/**
 * This has to be created at server side
 */
class KafkaReporter(topic: String, bootstrapServers: String) extends LatencyReporter {

  private val producer = ProducerSingleton.getInstance(bootstrapServers)

  override def report(startTime: Long, endTime: Long): Unit = {
    producer.send(new ProducerRecord[String, String](topic, null, s"$startTime:$endTime"))
  }
}

object ProducerSingleton {
  @volatile private var instance : Option[KafkaProducer[String, String]] = None

  def getInstance(bootstrapServers: String): KafkaProducer[String, String] = synchronized {
    if (!instance.isDefined) {
      synchronized {
        if(!instance.isDefined) {

          val props = new Properties()
          // kerberos security -- ldh 2024-12-17 15:27:08
          try {
            val currentDir = System.getProperty("user.dir")
            val security_props = new Properties
            security_props.load(new FileInputStream(currentDir + File.separator + "/conf/security.properties"))
            if (security_props.getProperty("iskerberos") == "true") {
              props.setProperty("sasl.kerberos.service.name", "kafka")
              props.setProperty("sasl.mechanism", "GSSAPI")
              props.setProperty("security.protocol", "SASL_PLAINTEXT")
              props.setProperty("sasl.jaas.config", security_props.getProperty("kafka_jaas"))
              System.setProperty("java.security.auth.login.config", security_props.getProperty("java.security.auth.login.config"))
              System.setProperty("java.security.krb5.conf", security_props.getProperty("java.security.krb5.conf"))
            }
          } catch {
            case e: FileNotFoundException =>
              throw new RuntimeException(e)
            case e: IOException =>
              throw new RuntimeException(e)
          }

          props.put("bootstrap.servers", bootstrapServers)
          instance = Some(new KafkaProducer(props, new StringSerializer, new StringSerializer))
        }
      }
    }
    instance.get
  }
}