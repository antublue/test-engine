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
mvn clean package integration-test
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

## Getting Help

GitHub's Discussions is the current mechanism for help / support.

## Building

You need Java 8 or greater to build.

```shell
git clone https://github.com/antublue/test-engine
cd test-engine
./mvnw clean verify
```

## Known issues

IntelliJ doesn't properly handle all possible test selection scenarios from the Test Run window.

- https://youtrack.jetbrains.com/issue/IDEA-317561/IntelliJ-test-method-selections-fails-for-hierarchical-test-in-test-output-window

IntelliJ doesn't properly display the correct test class display name when a single test class is selected.

- https://youtrack.jetbrains.com/issue/IDEA-318733/IntelliJ-test-class-display-name-is-incorrect-when-selecting-a-specific-test-class

IntelliJ doesn't properly display `System.out` / `System.err` for running tests.

- Various reports on https://youtrack.jetbrains.com


- The output can be misleading and should not be used as a source of truth.

## Contributing

Contributions to the Test Engine are both welcomed and appreciated.

The project uses a [GitHub Flow](https://docs.github.com/en/get-started/quickstart/github-flow) branching strategy.

- The [main](/) branch contains the latest unreleased code
- Google checkstyle format is required
- PMD is used for static analysis
- Expand all Java imports
- Tags are used for releases

For changes, you should...

- Fork the repository
- Create a branch for your work
- Make changes on your branch
- Build and test your changes
- Open a pull request, tagging `@antublue` for review
- A [Developer Certificate of Origin](DCO.md) (DCO) is required for all contributions

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

## FAQ

### Why not use JUnit 5?

Currently, JUnit 5 does not support parameterized testing at the test class level (common for parameterized integration testing.)

- https://github.com/junit-team/junit5/issues/878 (Open since 2017-06-09)


- It doesn't provide annotations to run methods before / after all test methods and before / after each test argument. (2023-04-18)


- It doesn't provide the detailed information during testing or summary information typically wanted for parameterized integration testing.

### Why not use JUnit 4?

JUnit 4 does provide test class level parameterization via `@RunWith(Parameterized.class)`.

- JUnit 4 is maintained, but no new functionality is being added.
  - The latest release was on February 13, 2021.
  - https://stackoverflow.com/questions/72325172/when-is-junit4-going-to-be-deprecated


- It doesn't provide annotations to run methods before / after all test methods and before / after each test argument.


- It uses a test class constructor approach, limiting you to Java inheritance semantics. (superclass before subclass construction.)


- It doesn't provide the detailed information during testing or summary information typically wanted for parameterized integration testing.

### What is parameterized integration testing?

Parameterized integration testing is most common when you...

1. Want to perform integration testing of the application in various environments.


2. You want to test workflow oriented scenarios of the application.


3. Various environments could involve different operating systems versions and/or different application runtime versions.

### How to lock shared resources?

The test engine runs multiple test classes in parallel (arguments within a test class are tested sequentially.)

For some test scenarios, shared resources may need to be used.

The test engine provides the `@TestEngine.Lock` and `@TestEngine.Unlock` annotations for resource locking.

Examples:

- [ClassLockingTest1.java](/examples/src/test/java/example/locking/ClassLockingTest1.java)
- [ClassLockingTest2.java](/examples/src/test/java/example/locking/ClassLockingTest2.java)
- [MethodLockingTest1.java](/examples/src/test/java/example/locking/MethodLockingTest1.java)
- [MethodLockingTest2.java](/examples/src/test/java/example/locking/MethodLockingTest2.java)
- [MultipleMethodsLockingTest1.java](/examples/src/test/java/example/locking/MultipleMethodsLockingTest1.java)
- [MultipleMethodsLockingTest2.java](/examples/src/test/java/example/locking/MultipleMethodsLockingTest2.java)

**Notes**

- Locks are reentrant

### I want to use the test engine in place of JUnit 5

You can use the test engine in place of Junit 5 in scenarios where you don't really have parameterized test classes.

- It's not a "drop in" replacement.
- Reference the [Design](https://github.com/antublue/test-engine#design) for the state machine flow.

Example:

- [Junit5ReplacementExampleTest.java](/examples/src/test/java/example/Junit5ReplacementExampleTest.java)
