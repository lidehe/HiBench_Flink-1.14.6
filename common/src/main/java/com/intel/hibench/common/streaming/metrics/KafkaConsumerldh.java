package com.intel.hibench.common.streaming.metrics;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class KafkaConsumerldh {

    public Properties props = new Properties();
    public static String CLIENT_ID = "metrics_reader";
    public String topic = "";
    public int partition = 0;
    public static AdminClient adminClient;


    public KafkaConsumerldh(String zookeeperConnect, String topic, int partition){
        props.put("bootstrap.servers", zookeeperConnect);
        props.put("group.id", CLIENT_ID);
    }


    public static  KafkaConsumer createConsumer(String bootstrapServer, String topic, int partition){

        try  {
            Properties adminProps = new Properties();
            Properties consumerProps = new Properties();
            String currentDir = System.getProperty("user.dir");
            Properties security_props = new Properties();
            security_props.load(new FileInputStream(currentDir + File.separator + "/conf/security.properties"));
            if (security_props.getProperty("iskerberos").equals("true")) {
                adminProps.setProperty("sasl.kerberos.service.name", "kafka");
                adminProps.setProperty("sasl.mechanism", "GSSAPI");
                adminProps.setProperty("security.protocol", "SASL_PLAINTEXT");
                adminProps.setProperty("sasl.jaas.config", security_props.getProperty("kafka_jaas"));

                consumerProps.setProperty("sasl.kerberos.service.name", "kafka");
                consumerProps.setProperty("sasl.mechanism", "GSSAPI");
                consumerProps.setProperty("security.protocol", "SASL_PLAINTEXT");
                consumerProps.setProperty("sasl.jaas.config", security_props.getProperty("kafka_jaas"));
            }

            adminProps.setProperty("bootstrap.servers",bootstrapServer);
            adminClient = AdminClient.create(adminProps);
            TopicPartitionInfo topicPartitionInfo = adminClient.describeTopics(Collections.singleton(topic)).all().get().get(topic).partitions().get(partition);
            Node leader = topicPartitionInfo.leader();
            System.out.println("leader is: " + leader.host() + " " + leader.port());
            TopicPartition topicPartition = new TopicPartition(topic,partition);

            consumerProps.setProperty("bootstrap.servers", leader.host() + ":" + leader.port());
            consumerProps.setProperty(ConsumerConfig.CLIENT_ID_CONFIG, CLIENT_ID);
            consumerProps.setProperty("group.id", "matrics");
            consumerProps=ConsumerConfig.addDeserializerToConfig(consumerProps, new StringDeserializer(), new StringDeserializer());
            KafkaConsumer consumer = new KafkaConsumer(consumerProps);
            consumer.assign(Collections.singletonList(topicPartition));
            return consumer;

        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Iterator<ConsumerRecord> iterator(KafkaConsumer consumer){
         Iterator<ConsumerRecord> iterator= consumer.poll(Duration.ofMillis(1000)).iterator();
        return iterator;
    }

    public static void close(){
        adminClient.close();
    }

}
