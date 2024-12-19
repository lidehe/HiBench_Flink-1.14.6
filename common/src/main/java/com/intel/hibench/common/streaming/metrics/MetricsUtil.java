package com.intel.hibench.common.streaming.metrics;

import com.intel.hibench.common.streaming.Platform;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class MetricsUtil {

    public static String getTopic(Platform platform,String sourceTopic,int producerNum,long recordPerInterval,int intervalSpan) {
        return platform+"_"+sourceTopic+"_"+producerNum+"_"+recordPerInterval+"_"+intervalSpan+"_"+System.currentTimeMillis();
    }

    public static void createTopic(String brokerList, String topic, int partitions) {
        Properties props = new Properties();
        props.setProperty("bootstrap.servers",brokerList);
        // kerberos security -- ldh 2024-10-26 00:10:22
        try {
            String currentDir = System.getProperty("user.dir");
            Properties security_props = new Properties();
            security_props.load(new FileInputStream(currentDir + File.separator + "/conf/security.properties"));
            if (security_props.getProperty("iskerberos").equals("true")) {
                props.setProperty("sasl.kerberos.service.name", "kafka");
                props.setProperty("sasl.mechanism", "GSSAPI");
                props.setProperty("security.protocol", "SASL_PLAINTEXT");
                props.setProperty("sasl.jaas.config", security_props.getProperty("kafka_jaas"));
                System.setProperty("java.security.auth.login.config", security_props.getProperty("java.security.auth.login.config"));
                System.setProperty("java.security.krb5.conf", security_props.getProperty("java.security.krb5.conf"));
            }
        } catch  (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        AdminClient adminClient = AdminClient.create(props);
        try {
            List<NewTopic> topicList=new ArrayList<NewTopic>();
            NewTopic newTopic = new NewTopic(topic, partitions, (short) 1);
            topicList.add(newTopic);
            adminClient.createTopics(topicList).all();
            ListTopicsResult result = adminClient.listTopics();
            while (!result.names().get().contains(topic)){
                result = adminClient.listTopics();
                Thread.sleep(100);
            }
            System.out.println("MetricsUtil about to create topic: " + topic+" ,with partition:"+partitions);
        } catch  (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            adminClient.close();
        }
    }
}
