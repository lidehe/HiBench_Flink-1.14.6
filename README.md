* Base on  HiBench-7.1.1
* Homepage: https://github.com/intel-hadoop/HiBench
* Contents:
  1. Overview
  2. Getting Started
  3. Workloads
  4. Supported Releases

---
### OVERVIEW ###
1.增加了对flink-1.14.6_2.11.12的支持
2.增加了对kafka-2.7.2的支持
3.增加了对开启kerberos集群的支持，在conf/security.properties中配置kerberos相关的参数

### Getting Started ###
 * [Build HiBench](docs/build-hibench.md)
 * [Run HadoopBench](docs/run-hadoopbench.md)
 * [Run SparkBench](docs/run-sparkbench.md)
 * [Run StreamingBench](docs/run-streamingbench.md) (Spark streaming, Flink, Storm, Gearpump)

### Supported Hadoop/Spark/Flink/Storm/Gearpump releases: ###

  - Hadoop: Apache Hadoop 3.0.x, 3.1.x, 3.2.x, 2.x, CDH5, HDP
  - Spark: Spark 2.4.x, Spark 3.0.x, Spark 3.1.x
  - Flink: 1.14.6_scala_2.11.12
  - Storm: 1.0.1
  - Gearpump: 0.8.1
  - Kafka: 2.7.2





