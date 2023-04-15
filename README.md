[![Build](https://github.com/antublue/test-engine/actions/workflows/build.yml/badge.svg)](https://github.com/antublue/test-engine/actions/workflows/build.yml)
[![Code Quality](https://api.codiga.io/project/36230/score/svg)](https://app.codiga.io/hub/project/36230/test-engine)
[![Code Grade](https://api.codiga.io/project/36230/status/svg)](https://app.codiga.io/hub/project/36230/test-engine)

<br/>

![AntuBLUE logo](resources/logo.png)

# Test Engine

The Test Engine is a JUnit 5 based test engine that allows for parameterized testing at the test class level.

## Goals

The goal of the Test Engine is for integration testing

Currently, JUnit 5 does not support parameterized tests at the test class level (common for integration testing)

- https://github.com/junit-team/junit5/issues/878

## Latest Releases

- General Availability (GA): [Test Engine v3.0.1](https://github.com/antublue/test-engine/releases/tag/v3.0.1)

## Common Annotations

| Annotation                      | Scope             | Required     | Static | Example                                                                          |
|---------------------------------|-------------------|--------------|--------|----------------------------------------------------------------------------------|
| `@TestEngine.ParameterSupplier` | method            | yes          | yes    | `public static Stream<Parameter> parameters();`                                  |
| `@TestEngine.Parameter`         | field<br/> method | yes (either) | no     | `public Parameter parameter;`<br/> `public void parameter(Parameter parameter);` |
| `@TestEngine.BeforeClass`       | method            | no           | yes    | `public static void beforeClass();`                                              |
| `@TestEngine.BeforeAll`         | method            | no           | no     | `public void beforeAll();`                                                       |
| `@TestEngine.BeforeEach`        | method            | no           | no     | `public void beforeEach();`                                                      |
| `@TestEngine.Test`              | method            | yes          | no     | `public void test();`                                                            |
| `@TestEngine.AfterEach`         | method            | no           | no     | `public void afterEach();`                                                       |
| `@TestEngine.AfterAll`          | method            | no           | no     | `public void afterAll();`                                                        |
| `@TestEngine.AfterClass`        | method            | no           | yes    | `public static void afterClass();`                                               |

**Notes**

- `public` and `protected` methods are supported for `@TestEngine.X` annotations


- By default, methods are executed in alphabetical order based on a method name, regardless of where they are declared (class or superclasses)


- `@TestEngine.Order` can be used to control method order
  - Methods are sorted by the annotation value first, then alphabetically by the test method name
    - In scenarios where `@TestEngine.Order` values are duplicated, methods with the same value are sorted alphabetically
  - Method order is relative to other methods with the same annotation

## Additional Annotations

| Annotation                  | Scope             | Required | Usage                                                                              |
|-----------------------------|-------------------|----------|------------------------------------------------------------------------------------|
| `@TestEngine.Disabled`      | class<br/> method | no       | Marks a test class or method disabled                                              |
| `@TestEngine.BaseClass`     | class             | no       | Marks a test class as being a base test class (skips direct execution)             |
| `@TestEngine.Order(<int>)`  | method            | no       | Provides a way to order methods relative to other methods with the same annotation |
| `@TestEngine.Tag(<string>)` | class             | no       | Provides a way to tag a test class or test method                                  | 


**Notes**

- `@TestEngine.Order(<int>)` is inheritance agnostic


- Only one `@TestEngine.Tag(<string>)` is supported for a test class / test method


- It's recommended to use a tag string format of `/tag1/tag2/tag3/`

## Configuration Values

The Test Engine has seven configuration parameters

| Configuration                   | Type         | Java System Property                         | Environment Variable                         |
|---------------------------------|--------------|----------------------------------------------|----------------------------------------------|
| thread count                    | integer      | antublue.test.engine.thread.count            | ANTUBLUE_TEST_ENGINE_THREAD_COUNT            |
| test class name include filter  | regex string | antublue.test.engine.test.class.include      | ANTUBLUE_TEST_ENGINE_TEST_CLASS_INCLUDE      |
| test class name exclude filter  | regex string | antublue.test.engine.test.class.exclude      | ANTUBLUE_TEST_ENGINE_TEST_CLASS_EXCLUDE      |
| test method name include filter | regex string | antublue.test.engine.test.method.include     | ANTUBLUE_TEST_ENGINE_TEST_METHOD_INCLUDE     |
| test method name exclude filter | regex string | antublue.test.engine.test.method.exclude     | ANTUBLUE_TEST_ENGINE_TEST_METHOD_EXCLUDE     |
| test class tag include filter   | regex string | antublue.test.engine.test.class.tag.include  | ANTUBLUE_TEST_ENGINE_TEST_CLASS_TAG_INCLUDE  |
| test class tag exclude filter   | regex string | antublue.test.engine.test.class.tag.exclude  | ANTUBLUE_TEST_ENGINE_TEST_CLASS_TAG_EXCLUDE  |
| test method tag include filter  | regex string | antublue.test.engine.test.method.tag.include | ANTUBLUE_TEST_ENGINE_TEST_METHOD_TAG_INCLUDE |
| test method tag exclude filter  | regex string | antublue.test.engine.test.method.tag.exclude | ANTUBLUE_TEST_ENGINE_TEST_METHOD_TAG_EXCLUDE |

Using a combination of the properties (or environment variables) allows for including / excluding individual test classes / test methods

The Test Engine as two experimental configuration parameters

| Configuration                 | Type    | Default | Java System Property                                | Environment Variable                                |
|-------------------------------|---------|---------|-----------------------------------------------------|-----------------------------------------------------|
| disable console TEST messages | boolean | true    | antublue.test.engine.experimental.log.test.messages | ANTUBLUE_TEST_ENGINE_EXPERIMENTAL_LOG_TEST_MESSAGES |
| disable console PASS messages | boolean | true    | antublue.test.engine.experimental.log.pass.messages | ANTUBLUE_TEST_ENGINE_EXPERIMENTAL_LOG_PASS_MESSAGES |

**Notes**

- Environment variables take precedence over Java system properties

- If all test methods are excluded, then the test class will be excluded

- If no test classes are found, an error exit code of -2 is returned

- Experimental configuration values are subject to change at any time


## What is a `Parameter` ?

`Parameter` is an interface all parameter objects must implement to allow for parameter name and value resolution

The `Parameter` interface also has static methods to wrap an Object

### Usage of `Parameter`

- `@TestEngine.ParametersSupplier` must return a `Stream<Parameter>`


- `@TestEngine.Parameter` methods requires single `Parameter` object as parameter


- `@TestEngine.Parameter` field is a single `Parameter` object


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
                "Array [" + i + "]",
                new String[] { String.valueOf(i), String.valueOf(i * 2) }));
    }
    return collection.stream();
}
```

In this scenario, the value of the `Parameter` is a String[] array

```java
String[] values = parameter.value();
```

## Example Test Class

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

Additional test examples...

https://github.com/antublue/test-engine/tree/main/examples/src/test/java/example

## Maven Usage

Disable the Maven Surefire plugin...

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.0.1</version>
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
    <version>3.0.1</version>
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
        <version>3.0.1</version>
    </dependency>
    <dependency>
        <groupId>org.antublue</groupId>
        <artifactId>test-engine</artifactId>
        <version>3.0.1</version>
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

- The `test-engine-api`, `test-engine`, and `test-engine-maven-plugin` versions must match starting with 3.0.1

Build and test your project...

```bash
mvn clean package integration-test
```

**Notes**

- The Test Engine requires core JUnit 5 jars as dependencies

## Design

State Machine flow...

```
 Scan all classpath jars for test classes that contains a method annotated with "@TestEngine.Test"
 
 for (each test class in the Collection<Class>) {
 
    for each test class, create a thread
    
    thread {
    
        call "@TestEngine.ParameterSupplier" method to get a Stream<Parameter>
    
        execute "@TestEngine.BeforeClass" methods 
     
        create a single instance of the test class
        
        for (each Parameter in the Stream<Parameter>) {
        
            execute the "@TestEngine.Parameter" method with the Parameter object
            
            execute "@TestEngine.BeforeAll" methods
            
            for (each "@TestEngine.Test" method in the test class) {
            
                execute "@TestEngine.BeforeEach" methods
            
                execute "@TestEngine.Test" method
                
                execute "@TestEngine.AfterEach" methods
            }
            
            execute "@TestEngine.AfterAll" method
        }
        
        execute "@TestEngine.AfterClass" methods
    }
 }
```

**Notes**

- Each parameterized test class will be executed sequentially, but different test classes are executed in parallel threads
  - By default, thread count is equal to number of available processors as reported by Java
  - The thread count can be changed by the Java system property or environment variable

# Building

You need Java 8 or greater to build

```shell
git clone https://github.com/antublue/test-engine
cd test-engine
mvn clean package
```

To install to your local repository

```shell
mvn clean package install
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
- Create a branch based on `development-<NEXT RELEASE>`
- Make your changes
- Open a PR against `development-<NEXT RELEASE>`

**Notes**

- Snapshots are not used


- The goal of the `development-<NEXT RELEASE>` branch is to be buildable/deployable as the next release