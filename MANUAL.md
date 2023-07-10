[![Build](https://github.com/antublue/test-engine/actions/workflows/build.yml/badge.svg)](https://github.com/antublue/test-engine/actions/workflows/build.yml)

<br/>

![AntuBLUE logo](assets/logo.png)

# Test Engine

The AntuBLUE Test Engine is a JUnit 5 based test engine designed specifically for parameterized integration testing by allowing parameterization at the test class level.

### Versions prior to v4.2.3

Versions prior to `v4.2.3` should not be used due to a critical bug ...

- [#32](https://github.com/antublue/test-engine/issues/32) Tests may be skipped on a slow machine or scenarios where there are a large number of test classes

**Notes**

- `v4.x.x` tests, if using `Store`, will need to be changed to use the `v5.x.x` version of `Store`


- `v4.x.x` tests, if using `Fail`, will need to be changed to throw an `AssertionError` or other `RuntimeException`.


- `v4.x.x` deprecated classes were removed

## API

### Test Annotations

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

Reference the [Design](https://github.com/antublue/test-engine#design) for the test engine execution flow.

**Notes**

- `public` and `protected` methods are supported for `@TestEngine.X` annotations.


- By default, methods are executed in alphabetical order based on class/method name, regardless of where they are declared (class or superclasses.)


- `@TestEngine.Order` can be used to control test class order / test method order of execution.
  - Classes/methods are sorted by the order annotation value first, then alphabetically by the class name/method name.
  - The test method name can be changed by using the `@TestEngine.DisplayName` annotation.
  - Method order is relative to other methods in a test class with the same annotation regardless of superclass / subclass location.


- **Class execution order can't be guaranteed unless the test engine is configured for a single thread.**

### Additional Test Annotations

| Annotation                  | Scope            | Required | Usage                                                                                                                              |
|-----------------------------|------------------|----------|------------------------------------------------------------------------------------------------------------------------------------|
| `@TestEngine.Disabled`      | class<br/>method | no       | Marks a test class or method disabled                                                                                              |
| `@TestEngine.BaseClass`     | class            | no       | Marks a test class as being a base test class (skips direct execution)                                                             |
| `@TestEngine.Order(<int>)`  | class<br/>method | no       | Provides a way to specify class execution order and/or method execution order (relative to other methods with the same annotation) |
| `@TestEngine.Tag(<string>)` | class            | no       | Provides a way to tag a test class or test method                                                                                  | 
| `@TestEngine.DisplayName`   | class<br/>method | no       | Provides a way to override a test class or test method name display name                                                           |
| `@TestEngine.Lock`          | method           | no       | Provides a way to acquire a named `ReentrantLock` and lock it before method execution                                              |
| `@TestEngine.UnLock`        | method           | no       | Provides a way to acquire a named `ReentrantLock` and unlock it after method execution                                             |

**Notes**

- Abstract test classes are not executed.


- `@TestEngine.Order(<int>)` is inheritance agnostic (test class and super classes are treated equally.)


- `@TestEngine.Order(<int>)` is ignored for abstract and `@TestEngine.BaseClass` annotated classes.


- For `@TestEngine.Tag(<string>)` annotations, it's recommended to use a tag string format of `/tag1/tag2/tag3/`.


- By default, `@TestEngine.Lock` and `@TestEngine.Unlock` use a `ReentrantReadWriteLock`, locking the write lock.
  - You can add `mode=TestEngine.LockMode.READ` to use a read lock.

### What is an `Argument`?

`Argument` is an interface all argument objects must implement to provide a name.

There are standard argument implementations for common Java data types:

- `BooleanArgument`
- `ByteArgument`
- `CharArgument`
- `ShortArgument`
- `IntegerArgument`
- `LongArgument`
- `BigIntegerArgument`
- `FloatArgument`
- `DoubleArgument`
- `BigDecimalArgument`
- `StringArgument`

Additionally, there is an `ObjectArgument<T>` argument implementation that allows passing an arbitrary object as an argument.

**Notes**

- It's recommended to implement a test specific `Argument` object instead of using `ObjectArgument<T>` whenever possible.

### What is a `Store` ?

A `Store` is a thread-safe convenience object that allow sharing of Objects between tests.

- [Store.java](/api/src/main/java/org/antublue/test/engine/api/Store.java)

## Code Examples

The `examples` project contains various testing examples and scenarios.

- [examples](/examples/src/test/java/example)

### Usage in a project

The [Prometheus JMX Exporter](https://github.com/prometheus/jmx_exporter) uses the AntuBLUE Test Engine for integration testing.

- Prometheus JMX Exporter [integration_test_suite](https://github.com/prometheus/jmx_exporter/tree/main/integration_test_suite)

## Configuration

### Maven Configuration

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

Add the AntuBLUE Test Engine Maven Plugin...

```xml
<plugin>
  <groupId>org.antublue</groupId>
  <artifactId>test-engine-maven-plugin</artifactId>
  <version>5.0.1</version>
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

Add the AntuBLUE Test Engine jars...

```xml
<dependencies>
  <dependency>
    <groupId>org.antublue</groupId>
    <artifactId>test-engine-api</artifactId>
    <version>5.0.1</version>
  </dependency>
  <dependency>
    <groupId>org.antublue</groupId>
    <artifactId>test-engine</artifactId>
    <version>5.0.1</version>
    <scope>test</scope>
  </dependency>
</dependencies>
```

**Notes**

- The `test-engine-api`, `test-engine`, and `test-engine-maven-plugin` versions must match.

Build and test your project...

```bash
./mvnw clean package integration-test
```

### Test Engine Configuration

The test engine has seven configuration parameters.
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
Using a combination of the system properties (and/or environment variables) allows for including / excluding individual test classes / test methods.

### Experimental Test Engine Configuration

The test engine as two experimental configuration parameters.


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

- Environment variables take precedence over Java system properties.


- If all test methods are excluded, then the test class will be excluded.


- If no test classes are found, an exit code of `-2` is returned.


- Experimental configuration values are subject to change at any time.

## Test Engine Summary

When running via Maven in a Linux console, the test engine will report a summary.

Example

```bash
[INFO] ------------------------------------------------------------------------
[INFO] AntuBLUE Test Engine 5.0.1 Summary
[INFO] ------------------------------------------------------------------------
[INFO] Test Classes   :  64, PASSED :  64, FAILED : 0, SKIPPED : 0
[INFO] Test Methods   : 803, PASSED : 803, FAILED : 0, SKIPPED : 0
[INFO] ------------------------------------------------------------------------
[INFO] PASSED
[INFO] ------------------------------------------------------------------------
[INFO] Total Test Time : 41 ms (41 ms)
[INFO] Finished At     : 2023-07-01T10:06:53.169
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

The test engine is not meant to replace JUnit 5 for unit tests, but can be used.

### Logical Flow

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

- Each test class will be executed sequentially in a thread, but different test classes are executed in parallel threads.
  - By default, thread count is equal to number of available processors as reported by Java.
  - The thread count can be changed via a configuration value.