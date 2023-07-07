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
