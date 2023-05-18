[![Build](https://github.com/antublue/test-engine/actions/workflows/build.yml/badge.svg)](https://github.com/antublue/test-engine/actions/workflows/build.yml)

<br/>

![AntuBLUE logo](resources/logo.png)

# Test Engine

The Test Engine is a JUnit 5 based test engine designed specifically for parameterized integration testing by allowing parameterization at the test class level.

## Latest Releases

- General Availability (GA): [Test Engine v4.1.0](https://github.com/antublue/test-engine/releases/tag/v4.1.0)

**Notes**

- v3.x.x tests will have to be migrated to v4.x.x

## Goals

The Test Engine is designed specifically for parameterized integration testing.

## Non-Goals

The Test Engine is not meant to replace JUnit for unit tests.

## Why not use JUnit 5?

Currently, JUnit 5 does not support parameterized tests at the test class level (common for parameterized integration testing)

- https://github.com/junit-team/junit5/issues/878 (Open since 2017-06-09)


- It doesn't provide annotations to run methods before and after all tests and before / after a test parameter (2023-04-18)


- It doesn't provide the summary information typically wanted for parameterized integration testing

## Why not use Junit 4?

Junit 4 does provide test class level parameterization via `@RunWith(Parameterized.class)`

- It doesn't provide annotations to run methods before and after all tests and before / after a test parameter


- It uses a test class constructor approach, limiting you to Java inheritance semantics (superclass before subclass construction)


- It doesn't provide the summary information typically wanted for parameterized integration testing

## What is parameterized integration testing?

Parameterized integration testing is most common when you...

1. Want to perform integration testing of the application in various environments


2. You want to test workflow oriented scenarios of the application


- various environments could involve different operating systems versions and/or different application runtime versions

---

A text book example...

1. You have developed a networked based application using Docker: ApplicationX


2. You want to test that the behavior of the ApplicationX is various runtime environments


3. You want to reuse the Docker network for performance reasons


The parameters in this scenario are the various runtime environments

---

A reference example...

https://github.com/antublue/test-engine/blob/main/examples/src/test/java/example/testcontainers/KafkaTest.java

This test is testing functionality of an Apache Kafka Producer and Consumer against four Confluent Platform server versions

- The test is very basic, with a single test method that declare the logic client prooduce / consume logic, but you could test multiple scenarios using ordered test methods

- Test state between methods is stored in a `KafkaTestState` object

## Common Test Annotations

| Annotation                     | Static | Type   | Required | Example                                                                                                                                                                            |
|--------------------------------|--------|--------|----------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `@TestEngine.ArgumentSupplier` | yes    | method | yes      | <nobr>`public static Stream<[Object that implements Argument]> arguments();`</nobr><br/><br/><nobr>`public static Iterable<[Object that implements Argument]> arguments();`</nobr> |
| `@TestEngine.Argument`         | no     | field  | yes      | `public Argument argument;`                                                                                                                                                        |
| `@TestEngine.Prepare`          | no     | method | no       | `public void prepare();`                                                                                                                                                           | `public void prepare();`                                                         |
| `@TestEngine.BeforeAll`        | no     | method | no       | `public void beforeAll();`                                                                                                                                                         |
| `@TestEngine.BeforeEach`       | no     | method | no       | `public void beforeEach();`                                                                                                                                                        |
| `@TestEngine.Test`             | no     | method | yes      | `public void test();`                                                                                                                                                              |
| `@TestEngine.AfterEach`        | no     | method | no       | `public void afterEach();`                                                                                                                                                         |
| `@TestEngine.AfterAll`         | no     | method | no       | `public void afterAll();`                                                                                                                                                          |
| `@TestEngine.Conclude`         | no     | method | no       | `public void conclude();`                                                                                                                                                          |

Reference the [Design](https://github.com/antublue/test-engine#design) for the state machine flow

**Notes**

- `public` and `protected` methods are supported for `@TestEngine.X` annotations


- By default, methods are executed in alphabetical order based on a method name, regardless of where they are declared (class or superclasses)


- `@TestEngine.Order` can be used to control test method order
  - Methods are sorted by the annotation value first, then alphabetically by the test method name
    - In scenarios where `@TestEngine.Order` values are duplicated, methods with the same name are sorted alphabetically
    - The test method name can be changed by using the `@TestEngine.DisplayName` annotation
  - Method order is relative to other methods with the same annotation regardless of superclass / subclass location

## Additional Test Annotations

| Annotation                  | Scope            | Required | Usage                                                                              |
|-----------------------------|------------------|----------|------------------------------------------------------------------------------------|
| `@TestEngine.Disabled`      | class<br/>method | no       | Marks a test class or method disabled                                              |
| `@TestEngine.BaseClass`     | class            | no       | Marks a test class as being a base test class (skips direct execution)             |
| `@TestEngine.Order(<int>)`  | method           | no       | Provides a way to order methods relative to other methods with the same annotation |
| `@TestEngine.Tag(<string>)` | class            | no       | Provides a way to tag a test class or test method                                  | 
| `@TestEngine.DisplayName`   | class<br/>method | no       | Provides a way to override a test class or test method name display name           |


**Notes**

- Abstract test classes are not executed


- `@TestEngine.Order(<int>)` is inheritance agnostic (test class and super classes are treated equally)


- It's recommended to use a tag string format of `/tag1/tag2/tag3/`

## Usage Example

```java
package example;

import org.antublue.test.engine.api.ObjectArgument;
import org.antublue.test.engine.api.TestEngine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Example test
 */
public class ExampleTest {

  @TestEngine.Argument
  private ObjectArgument<String> objectArgument;

  @TestEngine.ArgumentSuplpier
  public static Stream<ObjectArgument<String>> arguments() {
    Collection<ObjectArgument<String>> collection = new ArrayList<>();
    
    for (int i = 0; i < 10; i++) {
      collection.add(ObjectArgument.of("String " + i));
    }
    
    return collection.stream();
  }
  
  @TestEngine.Prepare
  public void prepare() {
      // Create any global class state
  }

  @TestEngine.BeforeAll
  public void beforeAll() {
    System.out.println("beforeAll()");
  }

  @TestEngine.Test
  public void test1() {
    String value = objectArgument.value();
    System.out.println("test1(" + value + ")");
  }

  @TestEngine.Test
  public void test2() {
    String value = objectArgument.value();
    System.out.println("test2(" + value + ")");
  }

  @TestEngine.AfterAll
  public void afterAll() {
    System.out.println("afterAll()");
  }
  
  @TestEnging.Conclude
  public void conclude() {
    // Clean up any global class state
  }
}
```

Real integration test example using `testcontainers-java` and Apache Kafka ...

https://github.com/antublue/test-engine/blob/main/examples/src/test/java/example/testcontainers/KafkaTest.java

Additional test examples...

https://github.com/antublue/test-engine/tree/main/examples/src/test/java/example

## What is an `Argument`?

`Argument` is an interface all arguments Objects must implement to provide a name

## What is an `ObjectArgument`?

- An `ObjectArgument` is a concrete class that implements `Argument` and allows passing an Object within an `Argument`


- The `ObjectArgument` class defines various static methods to wrap native Java types, using the value as the name
  - `boolean`
  - `byte`
  - `char`
  - `short`
  - `int`
  - `long`
  - `float`
  - `double`
  - `String`

Example

```java
@TestEngine.Argument
public ObjectArgument<String[]> objectArgument;

@TestEngine.ArgumentSupplier
public static Stream<ObjectArgument<String[]>> arguments() {
  Collection<ObjectArgument<String[]>> collection = new ArrayList<>();
  
  for (int i = 0; i < 10; i++) {
    collection.add(
      new ObjectArgument<>(
        "argument[" + i + "]", // name
        new String[] { String.valueOf(i), String.valueOf(i * 2) })); // value
  }
  
  return collection.stream();
}
```

In this scenario, the value of the `ObjectArgument` is a `String[]`

```java
String[] values = objectArgument.value();
```

## What is a `KeyValueStore` ?

A `KeyValueStore` is a `Map` like object that allows you to pass keys / values 


- It allows you to add keys/values using a fluent pattern


- It allows you to get a value cast to a specific type


```java
@TestEngine.Argument
public ObjectArgument<KeyValueStore> objectArgument;

@TestEngine.ArgumentSupplier
public static Stream<ObjectArgument<KeyValueStore>> arguments() {
  Collection<ObjectArgument<KeyValueStore>> collection = new ArrayList<>();
  
  for (int i = 0; i < 10; i++) {
    collection.add(
      new ObjectArgument<>(
        "argument[" + i + "]",
        new KeyValueStore().put("key" + i, "value" + i)));
  }
  
  return collection.stream();
}
```

```java

int value1 = objectArgument.value().get("value1");
int value2 = objectArgument.value().get("value2");
```

**Notes**

- `KeyValueStore` is provided as a quick convenience class to use in an `ObjectArgument`, but ideally you should create a custom `Argument` Object

- `KeyValueStore` can also be used to share test state between test methods, but ideally you should create a custom Object to store test state

## Test Engine Configuration

The Test Engine has seven configuration parameters
<br/>
<br/>

| Thread count         |                                   |
|----------------------|-----------------------------------|
| Environment variable | ANTUBLUE_TEST_ENGINE_THREAD_COUNT |
| System property      | antublue.test.engine.thread.count |
| Type                 | integer                           |
| Default              | number of processors              |

<br/>

| <nobr>Test class name include filter</nobr> |                                          |
|-------------------------------------------------|-----------------------------------------|
| Environment variable                            | ANTUBLUE_TEST_ENGINE_TEST_CLASS_INCLUDE |
| System property                                 | antublue.test.engine.test.class.include |
| Type                                            | regex string                            |

<br/>

| <nobr>Test class name exclude filter</nobr> |                                         |
|-------------------------------------------------|-----------------------------------------|
| Environment variable                            | ANTUBLUE_TEST_ENGINE_TEST_CLASS_EXCLUDE |
| System property                                 | antublue.test.engine.test.class.exclude |
| Type                                            | regex string                            |

<br/>

| <nobr>Test method name include filter</nobr> |                                          |
|--------------------------------------------------|------------------------------------------|
| Environment variable                             | ANTUBLUE_TEST_ENGINE_TEST_METHOD_INCLUDE |
| System property                                  | antublue.test.engine.test.method.include |
| Type                                             | regex string                             |

<br/>

| <nobr>Test method name exclude filter</nobr> |                                          |
|--------------------------------------------------|------------------------------------------|
| Environment variable                             | ANTUBLUE_TEST_ENGINE_TEST_METHOD_EXCLUDE |
| System property                                  | antublue.test.engine.test.method.exclude |
| Type                                             | regex string                             |

<br/>

| <nobr>Test class tag include filter</nobr> |                                             |
|------------------------------------------------|---------------------------------------------|
| Environment variable                           | ANTUBLUE_TEST_ENGINE_TEST_CLASS_TAG_INCLUDE |
| System property                                | antublue.test.engine.test.class.tag.include |
| Type                                           | regex string                                |

<br/>

| <nobr>Test class tag exclude filter</nobr> |                                             |
|------------------------------------------------|---------------------------------------------|
| Environment variable                           | ANTUBLUE_TEST_ENGINE_TEST_CLASS_TAG_EXCLUDE |
| System property                                | antublue.test.engine.test.class.tag.exclude |
| Type                                           | regex string                                |

<br/>

| <nobr>Test method tag include filter</nobr> |                                              |
|-------------------------------------------------|----------------------------------------------|
| Environment variable                            | ANTUBLUE_TEST_ENGINE_TEST_METHOD_TAG_INCLUDE |
| System property                                 | antublue.test.engine.test.method.tag.include |
| Type                                            | regex string                                 |

<br/>

| <nobr>Test method tag exclude filter</nobr> |                                              |
|-------------------------------------------------|----------------------------------------------|
| Environment variable                            | ANTUBLUE_TEST_ENGINE_TEST_METHOD_TAG_EXCLUDE |
| System property                                 | antublue.test.engine.test.method.tag.exclude |
| Type                                            | regex string                                 |

<br/>
Using a combination of the system properties (and/or environment variables) allows for including / excluding individual test classes / test methods

## Experimental Test Engine Configuration

The Test Engine as two experimental configuration parameters


| <nobr>Output console TEST messages</nobr> |                                                     |
|-----------------------------------------------|-----------------------------------------------------|
| Environment variable                          | ANTUBLUE_TEST_ENGINE_EXPERIMENTAL_LOG_TEST_MESSAGES |
| System property                               | antublue.test.engine.experimental.log.test.messages |
| Type                                          | boolean                                             |
| Default                                       | true                                                |

<br/>

| <nobr>Output console PASS / FAIL messages</nobr> |                                                     |
|--------------------------------------------------|-----------------------------------------------------|
| Environment variable                             | ANTUBLUE_TEST_ENGINE_EXPERIMENTAL_LOG_PASS_MESSAGES |
| System property                                  | antublue.test.engine.experimental.log.pass.messages |
| Type                                             | boolean                                             |
| Default                                          | true                                                |

<br/>

**Notes**

- Environment variables take precedence over Java system properties


- If all test methods are excluded, then the test class will be excluded


- If no test classes are found, an error exit code of -2 is returned


- Experimental configuration values are subject to change at any time


## Maven Configuration

Disable the Maven Surefire plugin...

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-surefire-plugin</artifactId>
  <version>3.1.0</version>
  <configuration>
    <skipTests>true</skipTests>
  </configuration>
</plugin>
```

Add the Test Engine Maven Plugin...

```xml
<plugin>
  <groupId>org.antublue</groupId>
  <artifactId>test-engine-maven-plugin</artifactId>
  <version>4.1.0</version>
  <executions>
    <execution>
      <phase>integration-test</phase>
      <goals>
        <goal>test</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```

Add the Test Engine jars (and dependencies)...

```xml
<dependencies>
  <dependency>
    <groupId>org.antublue</groupId>
    <artifactId>test-engine-api</artifactId>
    <version>4.1.0</version>
  </dependency>
  <dependency>
    <groupId>org.antublue</groupId>
    <artifactId>test-engine</artifactId>
    <version>4.1.0</version>
    <scope>test</scope>
  </dependency>
</dependencies>
```

**Notes**

- The `test-engine-api`, `test-engine`, and `test-engine-maven-plugin` versions must match

Build and test your project...

```bash
mvn clean package integration-test
```

## Test Engine Summary

When running via Maven in a Linux console, the Test Engine will report a summary similar to...

```bash
[INFO] ------------------------------------------------------------------------
[INFO] AntuBLUE Test Engine v4.1.0 Summary
[INFO] ------------------------------------------------------------------------
[INFO] Test Classes   :  17, PASSED :  17, FAILED : 0, SKIPPED : 0
[INFO] Test Argynebts : 119, PASSED : 119, FAILED : 0, SKIPPED : 0
[INFO] Test Methods   : 476, PASSED : 476, FAILED : 0, SKIPPED : 0
[INFO] ------------------------------------------------------------------------
[INFO] PASSED
[INFO] ------------------------------------------------------------------------
[INFO] Total Test Time : 1 minute, 24 seconds, 608 ms
[INFO] Finished At     : 2023-04-19T14:36:05.124
[INFO] ------------------------------------------------------------------------

```

Test Classes

- Total number of test classes tested

Test Arguments

- Total number of test arguments tested (all test classes)

Test Methods

- Total number of test methods tested (all parameters / all test classes)

**Notes**

- Test classes can/may different use different test parameters, so you can't necessarily extrapolate the math in reverse

## Building

You need Java 8 or greater to build

```shell
git clone https://github.com/antublue/test-engine
cd test-engine
./build.sh
```

## Known issues

IntelliJ doesn't properly handle all possible test selection scenarios from the Test Run window.

- https://youtrack.jetbrains.com/issue/IDEA-317561/IntelliJ-test-method-selections-fails-for-hierarchical-test-in-test-output-window

IntelliJ doesn't properly display the correct test class display name when a single test class is selected.

- https://youtrack.jetbrains.com/issue/IDEA-318733/IntelliJ-test-class-display-name-is-incorrect-when-selecting-a-specific-test-class

IntelliJ doesn't properly display `System.out` / `System.err` for running tests when "Track Running Tests" is enabled.

- Various reports on https://youtrack.jetbrains.com
- The output can be misleading and should not be used as a source of truth

## Getting Help

GitHub's Discussions is the current mechanism for help / support

## Contributing

Contributions to the Test Engine are both welcomed and appreciated.

The project uses a simplified GitFlow branching strategy

- `main` is the latest release
- `development-<NEXT RELEASE>` is the next release

For changes, you should...

- Fork the repository
- Create a branch based on `development-<NEXT RELEASE>`
- Make changes on your branch
- Open a PR against the source repository branch `development-<NEXT RELEASE>`

**Notes**

- Google checkstyle formatted code is required


- Snapshots are not used

## Design

Logical test execution flow...

```
Scan all classpath jars for test classes that contains a method annotated with "@TestEngine.Test"

for (each test class in the Collection<Class<?>>) {

  create a thread to test the test class (default thread count = machine processor count)
  
  thread {
  
    invoke the test class "@TestEngine.ArgumentSupplier" method to get a Stream<Argument> or Iterable<Argument>

    create a single instance of the test class  

    invoke the test instance "@TestEngine.Prepare" methods 
    
    for (each Argument) {
    
      set the test instance "@TestEngine.Argument" field to the Argument object
       
      invoke all test instance "@TestEngine.BeforeAll" methods
      
      for (each "@TestEngine.Test" method in the test class) {
      
        invoke all test instance "@TestEngine.BeforeEach" methods
    
        invoke the test instance "@TestEngine.Test" method
        
        invoke all test instance "@TestEngine.AfterEach" methods
      }
      
      invoke all test instance "@TestEngine.AfterAll" methods
    }
    
    invoke all test instance "@TestEngine.Conclude" methods
  }
}
```

**Notes**

- Each parameterized test class will be executed sequentially in a thread, but different test classes are executed in parallel threads
  - By default, thread count is equal to number of available processors as reported by Java
  - The thread count can be changed via a configuration value