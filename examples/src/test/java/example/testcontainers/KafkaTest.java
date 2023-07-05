package example.testcontainers;

import org.antublue.test.engine.api.TestEngine;
import org.antublue.test.engine.api.argument.StringArgument;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Example using testcontainers-java and Apache Kafka
 * <p>
 * Disabled by default since users may not have Docker installed
 */
@TestEngine.Disabled
public class KafkaTest {

    private static final String TOPIC = "test";
    private static final String GROUP_ID = "test-group-id";
    private static final String EARLIEST = "earliest";

    private KafkaTestState kafkaTestState;

    @TestEngine.Argument
    protected StringArgument stringArgument;

    @TestEngine.ArgumentSupplier
    public static Stream<StringArgument> arguments() {
        return Stream.of(
                StringArgument.of("confluentinc/cp-kafka:7.3.0"),
                StringArgument.of("confluentinc/cp-kafka:7.3.1"),
                StringArgument.of("confluentinc/cp-kafka:7.3.2"),
                StringArgument.of("confluentinc/cp-kafka:7.3.3"),
                StringArgument.of("confluentinc/cp-kafka:7.4.0"));
    }

    @TestEngine.Prepare
    public void prepare() {
        System.out.println("prepare()");

        Network network = Network.newNetwork();
        network.getId();

        kafkaTestState = new KafkaTestState();
        kafkaTestState.setNetwork(network);
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");

        Network network = kafkaTestState.getNetwork();
        String dockerImageName = stringArgument.value();

        KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse(dockerImageName));
        kafkaContainer.withNetwork(network);

        if (stringArgument.equals("confluentinc/cp-kafka:7.4.0")) {
            kafkaContainer.withKraft();
        } else {
            kafkaContainer.withEmbeddedZookeeper();
        }

        kafkaContainer.start();
        kafkaTestState.setKafkaContainer(kafkaContainer);

        String bootstrapServers = kafkaContainer.getBootstrapServers();
        kafkaTestState.setBootstrapServers(bootstrapServers);
    }

    @TestEngine.Test
    public void produceConsumeTest() throws Throwable {
        System.out.println("produceConsumeTest()");
        produce();
        consume();
    }

    private void produce() throws Throwable {
        String message = randomString(16);

        System.out.println(String.format("produce message [%s]", message));
        kafkaTestState.setMessage(message);

        String bootstrapServers = kafkaTestState.getBootstrapServers();

        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        KafkaProducer<String, String> producer = null;

        try {
            producer = new KafkaProducer<>(properties);
            ProducerRecord<String, String> producerRecord = new ProducerRecord<>(TOPIC, message);
            producer.send(producerRecord).get();
        } finally {
            if (producer != null) {
                producer.close();
            }
        }
    }

    private void consume() {
        String message = kafkaTestState.getMessage();
        String bootstrapServers = kafkaTestState.getBootstrapServers();

        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, EARLIEST);

        KafkaConsumer<String, String> consumer = null;

        try {
            List<String> topicList = Collections.singletonList(TOPIC);

            consumer = new KafkaConsumer<>(properties);
            consumer.subscribe(topicList);

            ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofMillis(1000));
            for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {
                System.out.println(String.format("consume message [%s]", consumerRecord.value()));
                assertThat(consumerRecord.value()).isEqualTo(message);
            }
        } finally {
            if (consumer != null) {
                consumer.close();
            }
        }
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
        kafkaTestState.close();
    }

    @TestEngine.Conclude
    public void conclude() {
        System.out.println("conclude()");
        kafkaTestState.close();
    }

    private static String randomString(int length) {
        return new Random()
                .ints(97, 123 + 1)
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    private static class KafkaTestState {

        private Network network;
        private KafkaContainer kafkaContainer;
        private String bootstrapServers;
        private String message;

        public void setNetwork(Network network) {
            this.network = network;
        }

        public Network getNetwork() {
            return network;
        }

        public void setKafkaContainer(KafkaContainer kafkaContainer) {
            this.kafkaContainer = kafkaContainer;
        }

        public KafkaContainer getKafkaContainer() {
            return kafkaContainer;
        }

        public void setBootstrapServers(String bootstrapServers) {
            this.bootstrapServers = bootstrapServers;
        }

        public String getBootstrapServers() {
            return bootstrapServers;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void close() {
            if (kafkaContainer != null) {
                kafkaContainer.close();
                kafkaContainer = null;
            }
        }

        public void dispose() {
            close();

            if (network != null) {
                network.close();;
                network = null;
            }
        }
    }
}
