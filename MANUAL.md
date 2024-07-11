[![Build](https://github.com/antublue/test-engine/actions/workflows/build.yml/badge.svg)](https://github.com/antublue/test-engine/actions/workflows/build.yml)

<br/>

![AntuBLUE logo](assets/logo.png)

# AntuBLUE Test Engine

The AntuBLUE test engine is a JUnit5 based test engine designed specifically for parameterized testing at the test class level.

## API

### Test Annotations

| Annotation                          | Static | Scope  | Required | Example                                                                                                                                                                                                                                  |
|-------------------------------------|--------|--------|----------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `@TestEngine.ArgumentSupplier`      | yes    | method | yes      | <nobr>`public static Stream<[Object that implements Argument]> arguments()`</nobr><br/><br/><nobr>`public static Iterable<[Object that implements Argument]> arguments()`</nobr><nobr>public static Argument<[Object]> argument()</nobr> |
| `@TestEngine.Argument`              | no     | field  |          | no       | `public Argument argument;`                                                                                                                                                                                                              |
| `@TestEngine.Prepare`               | no     | method | no       | `public void prepare();`                                                                                                                                                                                                                 |
| `@TestEngine.BeforeAll`             | no     | method | no       | `public void beforeAll();`                                                                                                                                                                                                               |
| `@TestEngine.BeforeEach`            | no     | method | no       | `public void beforeEach();`                                                                                                                                                                                                              |
| `@TestEngine.Test`                  | no     | method | yes      | `public void test();`                                                                                                                                                                                                                    |
| `@TestEngine.AfterEach`             | no     | method | no       | `public void afterEach();`                                                                                                                                                                                                               |
| `@TestEngine.AfterAll`              | no     | method | no       | `public void afterAll();`                                                                                                                                                                                                                |
| `@TestEngine.Conclude`              | no     | method | no       | `public void conclude();`                                                                                                                                                                                                                |
| `@TestEngine.ParallelArgumentTest`  | no     | class  | no       |                                                                                                                                                                                                                                          |

Reference the [Design](https://github.com/antublue/test-engine#design) for the test engine execution flow.

**Notes**

- Only `public` methods are supported for `@TestEngine.X` annotations.


- If `@TestEngine.ArgumentSupplier` returns an empty `Stream<Argument>`, `Iterable<Argument>`, or null, the test will be skipped.


- `@TestEngine.Order` can be used to control test class order / test method order of execution.
  - Classes/methods are sorted alphabetically first by test class name or display name, then by the order annotation value.

  
- **Class execution order can't be guaranteed unless the test engine is configured to use a single thread.**


- `@TestEngine.ParallelArgumentTest` is used to indicate to the test engine to test each argument in parallel
  - Annotated test classes **must be thread-safe**.
  - The Maven plugin test engine summary will treat the "split" classes as unique tests.

### Additional Test Annotations

| Annotation                                   | Scope            | Required | Usage                                                                                                                              |
|----------------------------------------------|------------------|----------|------------------------------------------------------------------------------------------------------------------------------------|
| `@TestEngine.Disabled`                       | class<br/>method | no       | Marks a test class or method disabled                                                                                              |
| `@TestEngine.Order(order = <int>)`           | class<br/>method | no       | Provides a way to specify class execution order and/or method execution order (relative to other methods with the same annotation) |
| `@TestEngine.Tag(tag = "<string>")`          | class            | no       | Provides a way to tag a test class or test method                                                                                  | 
| `@TestEngine.DisplayName(name = "<string>")` | class<br/>method | no       | Provides a way to override a test class or test method name display name                                                           |
| `@TestEngine.Random.Boolean`                 | field            | no       | Provides a way to inject a random boolean value                                                                                    |
| `@TestEngine.Random.Byte`                    | field            | no       | Provides a way to inject a random byte value                                                                                       |
| `@TestEngine.Random.Short`                   | field            | no       | Provides a way to inject a random short value                                                                                      |
| `@TestEngine.Random.Character`               | field            | no       | Provides a way to inject a random char value                                                                                       |
| `@TestEngine.Random.Integer`                 | field            | no       | Provides a way to inject a random integer value                                                                                    |
| `@TestEngine.Random.Long`                    | field            | no       | Provides a way to inject a random long value                                                                                       |
| `@TestEngine.Random.Float`                   | field            | no       | Provides a way to inject a random double value                                                                                     |
| `@TestEngine.Random.Double`                  | field            | no       | Provides a way to inject a random float value                                                                                      |
| `@TestEngine.Random.BigInteger`              | field            | no       | Provides a way to inject a random BigInteger value                                                                                 |
| `@TestEngine.Random.BigDecimal`              | field            | no       | Provides a way to inject a random BigDecimal value                                                                                 |
| `@TestEngine.Random.UUID`                    | field            | no       | Provides a way to inject a `UUID`                                                                                                  |

**Notes**

- Additional test annotations require non-static fields/methods

- `@TestEngine.Order(order = <int>)` is applies to methods defined in the class.


- `@TestEngine.Order(order = <int>)` is ignored for abstract test classes.


- For `@TestEngine.Tag(tag = "<string>")` annotations, it's recommended to use a tag string format of `/tag1/tag2/tag3/`.


- `@TestEngine.Random.X` annotated fields can be auto-converted to `String` values. 

- Abstract test classes are not executed.

## TestEngine Extension Annotations

The test engine has the ability to initialize/cleanup global resources before executing any tests.

```java
import org.antublue.test.engine.api.TestEngineExtension;

/** Example TestEngineExtensions */
public class ExampleTestEngineExtension implements TestEngineExtension {

    @Override
    public void prepareCallback() {
        System.out.println(getClass().getName() + ".prepare()");
    }

    @Override
    public void concludeCallback() {
        System.out.println(getClass().getName() + ".conclude()");
    }
}
```

### What is an `Argument`?

`Argument` is an interface all argument objects must implement to provide a name.

## Code Examples

The `examples` module contains various testing examples and scenarios.

- [examples](/examples/src/test/java/example)

### Usage in a project

The [Prometheus JMX Exporter](https://github.com/prometheus/jmx_exporter) uses the AntuBLUE test engine for integration testing.

- Prometheus JMX Exporter [integration_test_suite](https://github.com/prometheus/jmx_exporter/tree/main/integration_test_suite)

## Configuration

### Maven Configuration

Set up the Maven Surefire plugin to only run JUnit 5 tests...

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.2.5</version>
    <configuration>
        <includes>
            <include>%regex[.*JUnit5Test.*]</include>
        </includes>
        <systemPropertyVariables>
            <junit.jupiter.extensions.autodetection.enabled>true</junit.jupiter.extensions.autodetection.enabled>
        </systemPropertyVariables>
    </configuration>
</plugin>
```

Add the AntuBLUE test engine Maven Plugin...

```xml
<plugin>
    <groupId>org.antublue</groupId>
    <artifactId>test-engine-maven-plugin</artifactId>
    <version>7.x.x-SNAPSHOT</version>
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

Add the AntuBLUE test engine jars...

```xml
<dependencies>
  <dependency>
    <groupId>org.antublue</groupId>
    <artifactId>test-engine-api</artifactId>
    <version>TEST-ENGINE-VERSION</version>
  </dependency>
  <dependency>
    <groupId>org.antublue</groupId>
    <artifactId>test-engine</artifactId>
    <version>TEST-ENGINE-VERSION</version>
    <scope>test</scope>
  </dependency>
</dependencies>
```

**Notes**

- The `test-engine-api`, `test-engine`, and `test-engine-maven-plugin` versions must match.

Build and test your project...

```bash
./mvnw clean verify
```

### AntuBLUE Test Engine Configuration

The test engine uses a properties file (`antublue-test-engine.properties`) for configuration.

A recursive search is performed...

current directory `antublue-test-engine.properties`

-- parent directory `antublue-test-engine.properties`

---- parent directory `antublue-test-engine.properties`
      ...

Test engine properties can be overridden via environment variables.

Example:

`antublue-test-engine.properties` contains `antublue.test.engine.thread.count=10`

Export the property as an environment variable...

```bash
export ANTUBLUE_TEST_ENGINE_THREAD_COUNT=4
```

The test engine will use `4` threads.

### Standard AntuBLUE Test Engine properties

| Property                                           | Type    | Default value                    |
|----------------------------------------------------|---------|----------------------------------|
| antublue.test.engine.thread.type                   | string  | platform                         |
| antublue.test.engine.thread.count                  | integer | Max(1, number of processors - 2) |
| antublue.test.engine.test.class.include.regex      | string  |                                  |
| antublue.test.engine.test.class.exclude.regex      | string  |                                  |
| antublue.test.engine.test.method.include.regex     | string  |                                  |
| antublue.test.engine.test.method.exclude.regex     | string  |                                  |
| antublue.test.engine.test.class.tag.include.regex  | string  |                                  |
| antublue.test.engine.test.class.tag.exclude.regex  | string  |                                  |
| antublue.test.engine.test.method.tag.include.regex | string  |                                  |
| antublue.test.engine.test.method.tag.exclude.regex | string  |                                  |
| antublue.test.engine.test.class.shuffle            | boolean | false                            |
| antublue.test.engine.console.log.timing            | boolean | true                             |
| antublue.test.engine.console.log.timing.units      | string  | milliseconds                     |
| antublue.test.engine.console.log.test.messages     | boolean | true                             |
| antublue.test.engine.console.log.test.message      | string  | T                                |
| antublue.test.engine.console.log.skip.messages     | boolean | true                             |
| antublue.test.engine.console.log.skip.message      | string  | S                                |
| antublue.test.engine.console.log.pass.messages     | boolean | true                             |
| antublue.test.engine.console.log.pass.message      | string  | P                                |
| antublue.test.engine.console.log.fail.message      | string  | F                                |

**Notes**

`antublue.test.engine.thread.type` supports the values:

- `platform` (default value)
- `virtual`

`antublue.test.engine.console.log.timing.units` supports the following values:

- `nanoseconds`
- `microseconds`
- `milliseconds`
- `seconds`
- `minutes`

## Test Engine Summary

When running via Maven in a Linux console, the test engine will report a summaryEngineExecutionListener.

Example

```bash
[INFO] ------------------------------------------------------------------------
[INFO] AntuBLUE Test Engine <TEST-ENGINE-VERSION> Summary
[INFO] ------------------------------------------------------------------------
[INFO] Test Classes   :  89, PASSED :  89, FAILED : 0, SKIPPED : 0
[INFO] Test Arguments : 401, PASSED : 401, FAILED : 0, SKIPPED : 0
[INFO] Test Methods   : 829, PASSED : 829, FAILED : 0, SKIPPED : 0
[INFO] ------------------------------------------------------------------------
[INFO] TEST SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total Test Time : 5 seconds, 566 ms (5566.034 ms)
[INFO] Finished At     : 2023-08-31T00:32:12-04:00
[INFO] ------------------------------------------------------------------------

```

Test Classes

- Total number of test classes tested.

Test Methods

- Total number of test methods tested (all parameters / all test classes.)

## Known issues

IntelliJ doesn't properly handle all possible test selection scenarios from the Test Run window.

- https://youtrack.jetbrains.com/issue/IDEA-317561/IntelliJ-test-method-selections-fails-for-hierarchical-test-in-test-output-window

IntelliJ doesn't properly display the correct test class display name when a single test class is selected.

- https://youtrack.jetbrains.com/issue/IDEA-318733/IntelliJ-test-class-display-name-is-incorrect-when-selecting-a-specific-test-class

IntelliJ doesn't properly display `System.out` / `System.err` for running tests.

- Various reports on https://youtrack.jetbrains.com

- The output can be misleading and should not be used as a source of truth.

## Design

### Goals

To allow parameterized testing at the test class level, targeting integration testing.

### Non-Goals

The test engine is not meant to replace JUnit 5 for unit tests.

While it could be used, it's not recommend.

### Logical Flow (without extensions)

```
Scan all classpath jars for test classes that contains a method annotated with "@TestEngine.Test"

for (each test class in the Collection<Class<?>>) {

  create a thread to test the test class (default thread count = machine processor count)
  
  thread {
  
    invoke the test class "@TestEngine.ArgumentSupplier" method to get a Stream<Argument> or Iterable<Argument>

    create a single instance of the test class  

    invoke the test instance "@TestEngine.Prepare" methods 
    
    for (each Argument) {
    
      set the test instance "@TestEngine.Argument" annotated field (if it exists)
      
      invoke all test instance "@TestEngine.BeforeAll" methods
      
      for (each "@TestEngine.Test" method in the test class) {
      
        invoke all test instance "@TestEngine.BeforeEach" methods
    
        invoke the test instance "@TestEngine.Test" method
        
        invoke all test instance "@TestEngine.AfterEach" methods
      }
      
      invoke all test instance "@TestEngine.AfterAll" methods
      
      set the test instance "@TestEngine.Argument" annotated field to "null" (if it exists)
    }
    
    invoke all test instance "@TestEngine.Conclude" methods
  }
}
```

**Notes**

- Each test class will be executed sequentially in a thread, but different test classes are executed in parallel threads.
  - By default, thread count is equal to number of available processors as reported by Java - 2.
- The thread count can be changed via a configuration value.