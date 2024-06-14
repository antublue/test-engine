/*
 * Copyright (C) 2024 The AntuBLUE test-engine project authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package example.testcontainers;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.io.Closeable;
import java.util.Random;
import java.util.stream.Stream;
import org.antublue.test.engine.api.Argument;
import org.antublue.test.engine.api.TestEngine;
import org.bson.Document;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.DockerImageName;

/**
 * Example using testcontainers-java and MongoDB
 *
 * <p>Disabled by default since users may not have Docker installed
 */
//@TestEngine.Disabled
public class MongoDBTest {

    private String name;

    private static Network network;

    @TestEngine.Argument public MongoDBTestEnvironment mongoDBTestEnvironment;

    @TestEngine.ArgumentSupplier
    public static Stream<MongoDBTestEnvironment> arguments() {
        return Stream.of(MongoDBTestEnvironment.of("mongo:4.0.10"));
    }

    @TestEngine.Prepare
    public void createNetwork() {
        info("creating network ...");

        network = Network.newNetwork();
        String id = network.getId();

        info("network [%s] created", id);
    }

    @TestEngine.BeforeAll
    public void startTestContainer() {
        info("starting test container ...");

        mongoDBTestEnvironment.start(network);

        info("test container started");
    }

    @TestEngine.Test
    @TestEngine.Order(order = 1)
    public void testInsert() throws Throwable {
        info("testing testInsert() ...");

        name = randomString(16);

        MongoClientSettings settings =
                MongoClientSettings.builder()
                        .applyConnectionString(
                                new ConnectionString(
                                        mongoDBTestEnvironment.getPayload().getConnectionString()))
                        .build();

        try (MongoClient mongoClient = MongoClients.create(settings)) {
            MongoDatabase database = mongoClient.getDatabase("test-db");
            MongoCollection<Document> collection = database.getCollection("users");
            Document document = new Document("name", name).append("age", 30);
            collection.insertOne(document);
        }

        System.out.format("name [%s] inserted", name).println();
    }

    @TestEngine.Test
    @TestEngine.Order(order = 2)
    public void testQuery() {
        info("testing testQuery() ...");

        MongoClientSettings settings =
                MongoClientSettings.builder()
                        .applyConnectionString(
                                new ConnectionString(
                                        mongoDBTestEnvironment.getPayload().getConnectionString()))
                        .build();

        try (MongoClient mongoClient = MongoClients.create(settings)) {
            MongoDatabase database = mongoClient.getDatabase("test-db");
            MongoCollection<Document> collection = database.getCollection("users");
            Document query = new Document("name", name).append("age", 30);
            Document result = collection.find(query).first();
            assertThat(result).isNotNull();
            assertThat(result.get("name")).isEqualTo(name);
        }
    }

    @TestEngine.AfterAll
    public void afterAll() {
        mongoDBTestEnvironment.close();
    }

    @TestEngine.Conclude
    public void conclude() {
        network.close();
    }

    /** Class to implement a TestContext */
    public static class MongoDBTestEnvironment implements Argument<MongoDBContainer>, Closeable {

        private final String dockerImageName;
        private MongoDBContainer mongoDBContainer;

        /**
         * Constructor
         *
         * @param dockerImageName the name
         */
        public MongoDBTestEnvironment(String dockerImageName) {
            this.dockerImageName = dockerImageName;
        }

        /**
         * Method to get the name
         *
         * @return the name
         */
        @Override
        public String getName() {
            return dockerImageName;
        }

        /**
         * Method to get the payload (ourself)
         *
         * @return the payload
         */
        @Override
        public MongoDBContainer getPayload() {
            return mongoDBContainer;
        }

        /**
         * Method to start the MongoDBTestEnvironment using a specific network
         *
         * @param network the network
         */
        public void start(Network network) {
            info("test container [%s] starting ...", dockerImageName);

            mongoDBContainer = new MongoDBContainer(DockerImageName.parse(dockerImageName));
            mongoDBContainer.withNetwork(network);
            mongoDBContainer.start();

            info("test container [%s] started", dockerImageName);
        }

        /** Method to close (shutdown) the MongoDBTestEnvironment */
        public void close() {
            info("test container [%s] stopping ..", dockerImageName);

            if (mongoDBContainer != null) {
                mongoDBContainer.stop();
                mongoDBContainer = null;
            }

            info("test container [%s] stopped", dockerImageName);
        }

        /**
         * Method to create a MongoDBTestEnvironment
         *
         * @param dockerImageName the name
         * @return a MongoDBTestEnvironment
         */
        public static MongoDBTestEnvironment of(String dockerImageName) {
            return new MongoDBTestEnvironment(dockerImageName);
        }
    }

    /**
     * Method to create a random string
     *
     * @param length length
     * @return a random String
     */
    private static String randomString(int length) {
        return new Random()
                .ints(97, 123 + 1)
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    /**
     * Method to print an info print
     *
     * @param object object
     */
    private static void info(Object object) {
        System.out.println(object);
    }

    /**
     * Metod to print an info print
     *
     * @param format format
     * @param objects objects
     */
    private static void info(String format, Object... objects) {
        info(format(format, objects));
    }
}
