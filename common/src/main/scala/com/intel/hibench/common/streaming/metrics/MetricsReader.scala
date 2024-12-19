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

import com.intel.hibench.common.streaming.StreamBenchConfig

import java.io.{File, FileInputStream, IOException}
import java.util.Properties

object MetricsReader extends App {

  System.out.println("======================= here is MetricsReader ")

  if (args.length < 5) {
    System.err.println("args: <zookeeperConnect> <topic> <outputDir> <sampleNumber> <threadNumber> need to be specified!")
    System.exit(1)
  }

  // ldh 2024-10-30 09:10:03
//  val zookeeperConnect = args(0)
  val bootstrapServer = args(0)
  val topic = args(1)
  val outputDir = args(2)
  val sampleNum = args(3).toInt
  val threadNum = args(4).toInt
  // kerberos security -- ldh 2024-10-26 00:10:22
  try {
    val currentDir = System.getProperty("user.dir")
    val security_props = new Properties
    security_props.load(new FileInputStream(currentDir + File.separator + "/conf/security.properties"))
    if (security_props.getProperty("iskerberos")=="true") {
      System.setProperty("java.security.auth.login.config", security_props.getProperty("java.security.auth.login.config"))
      System.setProperty("java.security.krb5.conf", security_props.getProperty("java.security.krb5.conf"))
    }
  } catch {
    case e: IOException =>
      throw new RuntimeException(e)
  }

  System.out.println("======================= going to KafkaCollector ")
  val latencyCollector = new KafkaCollector(bootstrapServer, topic, outputDir, sampleNum, threadNum)
  latencyCollector.start()
}
