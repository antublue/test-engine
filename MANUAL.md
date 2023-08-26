[![Build](https://github.com/antublue/test-engine/actions/workflows/build.yml/badge.svg)](https://github.com/antublue/test-engine/actions/workflows/build.yml)

<br/>

![AntuBLUE logo](assets/logo.png)

# Test Engine

The AntuBLUE Test Engine is a JUnit 5 based test engine designed specifically for parameterized testing at the test class level.

## API

Test classes support both `Argument` injection (`@TestEngine.Argument` annotated field) and/or method an `Argument` for the following methods:

- `@TestEngine.BeforeAll`
- `@TestEngine.BeforeEach`
- `@TestEngine.Test` 
- `@TestEngine.AfterEach`
- `@TestEngine.AfterAll`

`@TestEngine.Argument` injection example:

- [SimpleTestExample.java](/examples/src/test/java/example/SimpleExampleTest.java)

`@TestEngine` without method injection example:

- [NoArgumentDeclarationTest.java](/examples/src/test/java/example/NoArgumentDeclarationTest.java)

### Test Annotations

| Annotation                     | Static | Type   | Required | Example                                                                                                                                                                            |
|--------------------------------|--------|--------|----------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `@TestEngine.ArgumentSupplier` | yes    | method | yes      | <nobr>`public static Stream<[Object that implements Argument]> arguments();`</nobr><br/><br/><nobr>`public static Iterable<[Object that implements Argument]> arguments();`</nobr> |
| `@TestEngine.Argument`         | no     | field  | no       | `public Argument argument;`                                                                                                                                                        |
| `@TestEngine.Prepare`          | no     | method | no       | `public void prepare();`                                                                                                                                                           |
| `@TestEngine.BeforeAll`        | no     | method | no       | `public void beforeAll();`                                                                                                                                                         |
| `@TestEngine.BeforeEach`       | no     | method | no       | `public void beforeEach();`                                                                                                                                                        |
| `@TestEngine.Test`             | no     | method | yes      | `public void test();` or `public void test(Argument argument)`                                                                                                                     |
| `@TestEngine.AfterEach`        | no     | method | no       | `public void afterEach();`                                                                                                                                                         |
| `@TestEngine.AfterAll`         | no     | method | no       | `public void afterAll();`                                                                                                                                                          |
| `@TestEngine.Conclude`         | no     | method | no       | `public void conclude();`                                                                                                                                                          |

Annotated methods ...

- `@TestEngine.Prepare`
- `@TestEngine.BeforeAll`
- `@TestEngine.BeforeAll`
- `@TestEngine.BeforeEach`
- `@TestEngine.AfterEach`
- `@TestEngine.AfterAll`
- `@TestEngine.Conclude`

... are execute using a wrapping model.

```
superclass @TestEngine.Prepare
  class @TestEngine.Prepare2
    superclass @TestEngine.BeforeAll
      class @TestEngine.BeforeAll2
        superclass @TestEngine.BeforeEach
          class @TestEngine.BeforeEach2
            superclass @TestEngine.Test
            class @TestEngine.Test2
          class @TestEngine.AfterEach2
        superclass @TestEngine.AfterEach
      class @TestEngine.AfterAll2
    superclass @TestEngine.AfterAll
  class @TestEngine.Conclude2
superclass @TestEngine.Conclude
```

Reference the [Design](https://github.com/antublue/test-engine#design) for the test engine execution flow.

**Notes**

- `public` and `protected` methods are supported for `@TestEngine.X` annotations.

- `@TestEngine.Order` can be used to control test class order / test method order of execution.
  - Classes/methods are sorted by the order annotation value first, then alphabetically by the class name/method name.
  - The test method name can be changed by using the `@TestEngine.DisplayName` annotation.
  - Method order is relative to the class (superclass or subclass) and other methods with the same annotation defined in the class.

- **Class execution order can't be guaranteed unless the test engine is configured to use a single thread.**

### Additional Test Annotations

| Annotation                                      | Scope            | Required | Usage                                                                                                                              |
|-------------------------------------------------|------------------|----------|------------------------------------------------------------------------------------------------------------------------------------|
| `@TestEngine.Disabled`                          | class<br/>method | no       | Marks a test class or method disabled                                                                                              |
| `@TestEngine.BaseClass`                         | class            | no       | Marks a test class as being a base test class (skips direct execution)                                                             |
| `@TestEngine.Order(order = <int>)`              | class<br/>method | no       | Provides a way to specify class execution order and/or method execution order (relative to other methods with the same annotation) |
| `@TestEngine.Tag(tag = "<string>")`             | class            | no       | Provides a way to tag a test class or test method                                                                                  | 
| `@TestEngine.DisplayName(name = "<string>")`    | class<br/>method | no       | Provides a way to override a test class or test method name display name                                                           |
| `@TestEngine.Lock(name = "<string>")`           | method           | no       | Provides a way to acquire a named lock, and lock it before method execution                                                        |
| `@TestEngine.Unlock(name = "<string>")`         | method           | no       | Provides a way to acquire a named lock, and unlock it after method execution                                                       |
| `@TestEngine.ResourceLock(name = "<string>")`   | method           | no       | Provides a way to acquire a named lock, locking it before method execution and unlocking it after method execution                 |
| `@TestEngine.AutoClose(lifecycle = "<string>")` | field            | no       | Provides a way to close `AutoCloseable` field                                                                                      |
| `@TestEngine.UUID`                              | field            | no       | Providate a way to inject a `UUID`                                                                                                 |
| `@TestEngine.RandomBoolean`                     | field            | no       | Provides a way to inject a random boolean value                                                                                    |
| `@TestEngine.RandomInteger`                     | field            | no       | Provides a way to inject a random integer value                                                                                    |
| `@TestEngine.RandomLong`                        | field            | no       | Provides a way to inject a random long value                                                                                       |
| `@TestEngine.RandomFloat`                       | field            | no       | Provides a way to inject a random double value                                                                                     |
| `@TestEngine.RandomDouble`                      | field            | no       | Provides a way to inject a random float value                                                                                      |
| `@TestEngine.RandomBigInteger`                  | field            | no       | Provides a way to inject a random BigInteger value                                                                                 |
| `@TestEngine.RandomBigDecimal`                  | field            | no       | Provides a way to inject a random BigDecimal value                                                                                 |

**Notes**

- Abstract test classes are not executed.


- `@TestEngine.Order(order = <int>)` is applies to methods defined in the class.


- `@TestEngine.Order(order = <int>)` is ignored for abstract and `@TestEngine.BaseClass` annotated classes.


- For `@TestEngine.Tag(tag = "<string>")` annotations, it's recommended to use a tag string format of `/tag1/tag2/tag3/`.


- By default, `@TestEngine.Lock`, `@TestEngine.Unlock`, and `@TestEngine.ResourceLock` use a `ReentrantReadWriteLock`, locking the write lock.
  - You can add `mode=TestEngine.LockMode.READ` to use a read lock.


- `@TestEngine.Lock`, `@TestEngine.Unlock`, and `@TestEngine.ResourceLock` are all repeatable.


- `@TestEngine.AutoClose` fields are processed after `@TestEngine.AfterEach`, `@TestEngine.AfterAll`, and `@TestEngine.Conclude` methods depending on lifecycle.
  - Lifecycle must be `@TestEngine.AfterEach`, `@TestEngine.AfterAll`, or `@TestEngine.Conclude`.
  - The annotation has an optional value `method` (Object method name) to call a method of an Object that doesn't implement `AutoCloseable`.


- `@TestEngine.UUID` can be used for either `UUID` or `String` field. 

- `@TestEngine.RandomInteger`, `@TestEngine.RandomLong`, `@TestEngine.RandomFloat`, and `@TestEngine.RandomDouble` all have optional minimum and maximum values.
  - The `minimum` and `maximum` values are inclusive.
  - If `minimum` is greater than `maximum`, then the values are swapped to create a valid range.

### What is an `Argument`?

`Argument` is an interface all argument objects must implement to provide a name.

There are standard argument implementations for common Java data types:

-  `BooleanArgument`
-  `ByteArgument`
-  `CharArgument`
-  `ShortArgument`
-  `IntegerArgument`
-  `LongArgument`
-  `BigIntegerArgument`
-  `FloatArgument`
-  `DoubleArgument`
-  `BigDecimalArgument`
-  `StringArgument`

Additionally, there is an `ObjectArgument<T>` argument implementation that allows passing an arbitrary object as an argument.

**Notes**

- It's recommended to implement a test specific `Argument` object instead of using `ObjectArgument<T>` whenever possible.

### What is a `Store` ?

A `Store` is a thread-safe convenience class that allow sharing of Objects between tests.

- [Store.java](/api/src/main/java/org/antublue/test/engine/api/Store.java)

## Code Examples

Very simple example.

- [SimpleTestExample.java](/examples/src/test/java/example/SimpleExampleTest.java)

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
  <version>TEST-ENGINE-VERSION</version>
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
./mvnw clean package integration-test
```

### Test Engine Configuration

The test engine uses a properties file for configuration.

The properties filename is resolved using the following in order:

- Environment variable `ANTUBLUE_TEST_ENGINE_PROPERTIES`


- System property `antublue.test.engine.properties`

 
- Default file `./.antublue-test-engine.properties` (current directory)


- Default file `~/.antublue-test-engine.properties` (home directory)

### Standard Test Engine properties

| Property                                       | Type         | Default value        |
|------------------------------------------------|--------------|----------------------|
| antublue.test.engine.thread.count              | integer      | number of processors |
| antublue.test.engine.stack.trace.pruning       | boolean      | true                 |
| antublue.test.engine.test.class.include        | regex string |                      |
| antublue.test.engine.test.class.exclude        | regex string |                      |
| antublue.test.engine.test.method.include       | regex string |                      |
| antublue.test.engine.test.class.tag.include    | regex string |                      |
| antublue.test.engine.test.class.tag.exclude    | regex string |                      |
| antublue.test.engine.test.method.tag.include   | regex string |                      |
| antublue.test.engine.test.method.tag.exclude   | regex string |                      |
| antublue.test.engine.test.class.shuffle        | boolean      | false                |
| antublue.test.engine.console.log.timing        | boolean      | true                 |
| antublue.test.engine.console.log.timing.units  | string       | milliseconds         |
| antublue.test.engine.console.log.test.messages | boolean      | true                 |

**Notes**

`antublue.test.engine.console.log.timing.units` supports the following values:

- `nanoseconds`
- `microseconds`
- `milliseconds`
- `seconds`
- `minutes`

### Experimental Test Engine Configuration

| Property                                                    | Type    | Default value |
|-------------------------------------------------------------|---------|---------------|
| antublue.test.engine.experimental.console.log.pass.messages | boolean | true          |

**Notes**

- Experimental configuration values are subject to change at any time.

## Test Engine Summary

When running via Maven in a Linux console, the test engine will report a summary.

Example

```bash
[INFO] ------------------------------------------------------------------------
[INFO] AntuBLUE Test Engine <VERSION> Summary
[INFO] ------------------------------------------------------------------------
[INFO] Test Classes :  62, PASSED :  62, FAILED : 0, SKIPPED : 0
[INFO] Test Methods : 791, PASSED : 791, FAILED : 0, SKIPPED : 0
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
  - By default, thread count is equal to number of available processors as reported by Java.
- The thread count can be changed via a configuration value.