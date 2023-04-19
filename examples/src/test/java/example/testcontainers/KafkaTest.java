package example.testcontainers;

import org.antublue.test.engine.api.Parameter;
import org.antublue.test.engine.api.TestEngine;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Example using testcontainers-java and Apache Kafka
 */
public class KafkaTest {

    private static Network network;

    private Parameter parameter;

    private KafkaContainer kafkaContainer;
    private String bootstrapServers;
    private String message;

    @TestEngine.ParameterSupplier
    public static Stream<Parameter> parameters() {
        return Stream.of(
                Parameter.of("confluentinc/cp-kafka:7.3.0"),
                Parameter.of("confluentinc/cp-kafka:7.3.1"),
                Parameter.of("confluentinc/cp-kafka:7.3.2"),
                Parameter.of("confluentinc/cp-kafka:7.3.3"));
    }

    @TestEngine.Parameter
    public void parameter(Parameter parameter) {
        this.parameter = parameter;
    }

    @TestEngine.BeforeClass
    public static void createNetwork() {
        System.out.println("createNetwork()");
        network = Network.newNetwork();
        network.getId();
    }

    @TestEngine.BeforeAll
    public void createKafkaServer() {
        System.out.println("createKafkaServer()");
        kafkaContainer = new KafkaContainer(DockerImageName.parse(parameter.value(String.class)));
        kafkaContainer.withNetwork(network);
        kafkaContainer.withEmbeddedZookeeper();
        kafkaContainer.start();
        bootstrapServers = kafkaContainer.getBootstrapServers();
    }

    @TestEngine.Test
    public void produceConsumeTest() throws Throwable {
        System.out.println("produceConsumeTest()");
        produce();
        consume();
    }

    private void produce() throws Throwable {
        message = randomString(16);
        System.out.println(String.format("produce message [%s]", message));

        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        KafkaProducer<String, String> producer = null;

        try {
            producer = new KafkaProducer<>(properties);
            ProducerRecord<String, String> producerRecord = new ProducerRecord<>("test", message);
            producer.send(producerRecord).get();
        } finally {
            if (producer != null) {
                producer.flush();
                producer.close();
            }
        }
    }

    private void consume() throws Throwable {
        String groupId = "test-group-id";
        String topic = "test";

        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        KafkaConsumer<String, String> consumer = null;

        try {
            List<String> topicList = new ArrayList<>();
            topicList.add(topic);

            consumer = new KafkaConsumer<>(properties);
            consumer.subscribe(topicList);

            ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofMillis(1000));
            for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {
                System.out.println(String.format("consume message key [%s] value [%s]", consumerRecord.key(), consumerRecord.value()));
                assertThat(consumerRecord.value()).isEqualTo(message);
            }
        } finally {
            if (consumer != null) {
                consumer.close();
            }
        }
    }

    @TestEngine.AfterAll
    public void destroyKafkaServer() {
        System.out.println("destroyKafkaServer()");
        if (kafkaContainer != null) {
            kafkaContainer.stop();
            kafkaContainer.close();
        }
    }

    @TestEngine.AfterClass
    public static void destroyNetwork() {
        System.out.println("destroyNetwork()");
        if (network != null) {
            network.close();
        }
    }

    private static String randomString(int length) {
        return new Random()
                .ints(97, 123 + 1)
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
