[![Build](https://github.com/antublue/test-engine/actions/workflows/build.yml/badge.svg)](https://github.com/antublue/test-engine/actions/workflows/build.yml)

<br/>

![AntuBLUE logo](resources/logo.png)

# Test Engine

The Test Engine is a JUnit 5 based test engine designed specifically for parameterized integration testing by allowing parameterization at the test class level.

## Latest Releases

- General Availability (GA): [Test Engine v3.0.4](https://github.com/antublue/test-engine/releases/tag/v3.0.4)

## Goals

The Test Engine is designed specifically for parameterized integration testing.

## Non-Goals

The Test Engine is not meant to replace JUnit for unit tests.

## Why not use JUnit 5?

Currently, JUnit 5 does not support parameterized tests at the test class level (common for parameterized integration testing)

- https://github.com/junit-team/junit5/issues/878


- The JUnit team is working on the functionality, but it's not clear if it will satisfy the common parameterized integration testing use cases (2023-04-18)


- It doesn't provide annotations to run static methods before/after the class **and** instance methods before/after all tests in a test class (2023-04-18)


- It doesn't provide the summary information typically wanted for parameterized integration testing

## Why not use Junit 4?

Junit 4 does provide test class level parameterization via `@RunWith(Parameterized.class)`

- It doesn't provide annotations to run static methods before/after the class **and** instance methods before/after all tests in a test class


- It doesn't provide the summary information typically wanted for parameterized integration testing

## What is parameterized integration testing?

Parameterized integration testing is most common when you...

1. Want to perform integration testing of the application in various environments


2. You want to test workflow oriented scenarios of the application


- various environments could involve different operation systems versions and/or different runtime versions

---

A text book example...

1. You have developed a networked based application: ApplicationX


2. You want to test that the behavior of the ApplicationX is various runtime environments


3. You want to test workflow scenarios


The parameters in this scenario are the various runtime environments

---

A reference example...

https://github.com/antublue/test-engine/blob/main/examples/src/test/java/example/testcontainers/KafkaTest.java

This test is testing functionality of an Apache Kafka Producer and Consumer against four Apache Kafka servers.

- The test is very basic, with a single test method that declare the logic client logic, but you could test multiple scenarios using ordered test methods


- This example is really testing Apache Kafka

## Common Test Annotations

| Annotation                      | Scope             | Required                               | Static | Example                                                                          |
|---------------------------------|-------------------|----------------------------------------|--------|----------------------------------------------------------------------------------|
| `@TestEngine.ParameterSupplier` | method            | yes                                    | yes    | <nobr>`public static Stream<Parameter> parameters();`</nobr>                     |
| `@TestEngine.Parameter`         | field<br/> method | yes <br/><nobr>(either or both)</nobr> | no     | `public Parameter parameter;`<br/> `public void parameter(Parameter parameter);` |
| `@TestEngine.BeforeClass`       | method            | no                                     | yes    | `public static void beforeClass();`                                              |
| `@TestEngine.BeforeAll`         | method            | no                                     | no     | `public void beforeAll();`                                                       |
| `@TestEngine.BeforeEach`        | method            | no                                     | no     | `public void beforeEach();`                                                      |
| `@TestEngine.Test`              | method            | yes                                    | no     | `public void test();`                                                            |
| `@TestEngine.AfterEach`         | method            | no                                     | no     | `public void afterEach();`                                                       |
| `@TestEngine.AfterAll`          | method            | no                                     | no     | `public void afterAll();`                                                        |
| `@TestEngine.AfterClass`        | method            | no                                     | yes    | `public static void afterClass();`                                               |

Reference the [Design](https://github.com/antublue/test-engine#design) for the state machine flow

**Notes**

- `public` and `protected` methods are supported for `@TestEngine.X` annotations


- By default, methods are executed in alphabetical order based on a method name, regardless of where they are declared (class or superclasses)


- `@TestEngine.Order` can be used to control test method order
  - Methods are sorted by the annotation value first, then alphabetically by the test method name
    - In scenarios where `@TestEngine.Order` values are duplicated, methods with the same name are sorted alphabetically
    - Declaration of test methods (class or superclass) are ignored
  - Method order is relative to other methods with the same annotation

## Additional Test Annotations

| Annotation                  | Scope             | Required | Usage                                                                              |
|-----------------------------|-------------------|----------|------------------------------------------------------------------------------------|
| `@TestEngine.Disabled`      | class<br/> method | no       | Marks a test class or method disabled                                              |
| `@TestEngine.BaseClass`     | class             | no       | Marks a test class as being a base test class (skips direct execution)             |
| `@TestEngine.Order(<int>)`  | method            | no       | Provides a way to order methods relative to other methods with the same annotation |
| `@TestEngine.Tag(<string>)` | class             | no       | Provides a way to tag a test class or test method                                  | 


**Notes**

- Abstract test classes are not executed


- `@TestEngine.Order(<int>)` is inheritance agnostic


- Only one `@TestEngine.Tag(<string>)` is supported for a test class / test method


- It's recommended to use a tag string format of `/tag1/tag2/tag3/`

## Usage Example

```java
package org.antublue.test.engine.test.example;

import api.org.antublue.test.engine.Parameter;
import api.org.antublue.test.engine.TestEngine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Example test
 */
public class ParameterTest {

  private Parameter parameter;

  @TestEngine.ParameterSupplier
  public static Stream<Parameter> parameters() {
    Collection<Parameter> collection = new ArrayList<>();

    for (int i = 0; i < 10; i++) {
      int value = i * 3;
      collection.add(Parameter.of("parameter(" + i + ") = " + value, String.valueOf(value)));
    }

    return collection.stream();
  }

  @TestEngine.Parameter
  public void parameter(Parameter parameter) {
    this.parameter = parameter;
  }

  @TestEngine.BeforeAll
  public void beforeAll() {
    System.out.println("beforeAll()");
  }

  @TestEngine.Test
  public void test1() {
    System.out.println("test1(" + parameter.value() + ")");
  }

  @TestEngine.Test
  public void test2() {
    System.out.println("test2(" + parameter.value() + ")");
  }

  @TestEngine.AfterAll
  public void afterAll() {
    System.out.println("afterAll()");
  }
}
```

Real integration test example using `testcontainers-java` and Apache Kafka ...

https://github.com/antublue/test-engine/blob/main/examples/src/test/java/example/testcontainers/KafkaTest.java

Additional test examples...

https://github.com/antublue/test-engine/tree/main/examples/src/test/java/example

## What is a `Parameter`?

`Parameter` is an interface all parameter objects must implement to allow for parameter name and value resolution

The `Parameter` interface also has static methods to wrap an Object


- `@TestEngine.ParameterSupplier` must return a `Stream<Parameter>`


- `@TestEngine.Parameter` field is a single `Parameter` object


- `@TestEngine.Parameter` methods requires single `Parameter` object as parameter


- The `Parameter` interface defines various static methods to wrap basic Java types, using the value as the name
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
@TestEngine.ParameterSupplier
public static Stream<Parameter> parameters() {
  Collection<Parameter> collection = new ArrayList<>();
  
  for (int i = 0; i < 10; i++) {
    collection.add(
      Parameter.of(
        "Array [" + i + "]", // name
          new String[] { String.valueOf(i), String.valueOf(i * 2) })); // value
  }
  
  return collection.stream();
}
```

In this scenario, the value of the `Parameter` is a String[] array

```java
String[] values = parameter.value();
```

## What is a `ParameterMap` ?

A `ParameterMap` is `Map` Object that implements the `Parameter` interface

- It allows you to add keys/values using a fluent pattern


- It allows you to get a value casted to a specific type


```java
@TestEngine.ParameterSupplier
public static Stream<Parameter> parameters() {
  Collection<Parameter> collection = new ArrayList<>();

  for (int i = 0; i < 10; i++) {
    collection.add(
      ParameterMap
        .named("Map[" + i + "]")
        .put("key", i)
        .parameter());
  }

  return collection.stream();
}
```

```java
String value = map.get("key");
```

```java
if (map.get("key", String.class).length() == 0) {
    // handle empty string
}
```

## Test Engine Configuration

The Test Engine has seven configuration parameters
<br/>
<br/>

| Thread count         |                                   |
|----------------------|-----------------------------------|
| Environment variable | ANTUBLUE_TEST_ENGINE_THREAD_COUNT |
| System property      | antublue.test.engine.thread.count |
| Type                 | integer                           |

<br/>

| <nobr>Test class name include filter</nobr> |                                         |
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
  <version>3.0.4</version>
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
  <version>3.0.4</version>
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
    <version>3.0.4</version>
  </dependency>
  <dependency>
    <groupId>org.antublue</groupId>
    <artifactId>test-engine</artifactId>
    <version>3.0.4</version>
    <scope>test</scope>
  </dependency>
  <dependency>
    <groupId>org.assertj</groupId>
    <artifactId>assertj-core</artifactId>
    <version>3.24.2</version>
    <scope>test</scope>
  </dependency>
  <dependency>
    <groupId>org.junit.platform</groupId>
    <artifactId>junit-platform-commons</artifactId>
    <version>1.9.2</version>
    <scope>test</scope>
  </dependency>
  <dependency>
    <groupId>org.junit.platform</groupId>
    <artifactId>junit-platform-launcher</artifactId>
    <version>1.9.2</version>
    <scope>test</scope>
  </dependency>
  <dependency>
    <groupId>org.junit.platform</groupId>
    <artifactId>junit-platform-engine</artifactId>
    <version>1.9.2</version>
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

**Notes**

- The Test Engine requires core JUnit 5 jars as dependencies

## Test Engine Summary

When running via Maven in a Linux console, the Test Engine will report a summary

```bash
[INFO] ------------------------------------------------------------------------
[INFO] AntuBLUE Test Engine v3.0.4 Summary
[INFO] ------------------------------------------------------------------------
[INFO] Test Classes    :  17, PASSED :  17, FAILED : 0, SKIPPED : 0
[INFO] Test Parameters : 119, PASSED : 119, FAILED : 0, SKIPPED : 0
[INFO] Test Methods    : 476, PASSED : 476, FAILED : 0, SKIPPED : 0
[INFO] ------------------------------------------------------------------------
[INFO] PASSED
[INFO] ------------------------------------------------------------------------
[INFO] Total Test Time : 1 minute, 24 seconds, 608 ms
[INFO] Finished At     : 2023-04-19T14:36:05.124
[INFO] ------------------------------------------------------------------------

```

Test Classes

- Total number of test classes tested

Test Parameters

- Total number of test parameters tested (all test classes)

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

The test execution flow...

```
Scan all classpath jars for test classes that contains a method annotated with "@TestEngine.Test"

for (each test class in the Collection<Class<?>>) {

  for each test class, create a thread (default thread count = machine processor count)
  
  thread {
  
      invoke the test class "@TestEngine.ParameterSupplier" method to get a Stream<Parameter>
  
      invoke the test class "@TestEngine.BeforeClass" methods 
   
      create a single instance of the test class
      
      for (each Parameter in the Stream<Parameter>) {
      
          set all test instance "@TestEngine.Parameter" fields to the Parameter object
      
          invoke all test instance "@TestEngine.Parameter" methods with the Parameter object
          
          invoke all test instance "@TestEngine.BeforeAll" methods
          
          for (each "@TestEngine.Test" method in the test class) {
          
              invoke all test instance "@TestEngine.BeforeEach" methods
          
              invoke the test instance "@TestEngine.Test" method
              
              invoke all test instance "@TestEngine.AfterEach" methods
          }
          
          invoke all test instance "@TestEngine.AfterAll" methods
      }
      
      invoke all test class "@TestEngine.AfterClass" methods
  }
}
```

**Notes**

- Each parameterized test class will be executed sequentially in a thread, but different test classes are executed in parallel threads
  - By default, thread count is equal to number of available processors as reported by Java
  - The thread count can be changed via a configuration value