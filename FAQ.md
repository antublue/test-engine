## FAQ

---

### Why not use JUnit 5?

Currently, JUnit 5 does not support parameterized testing at the test class level (common for integration testing.)

- https://github.com/junit-team/junit5/issues/878 (Open since 2017-06-09)


- It doesn't provide annotations to run methods before / after all test methods and before / after each test argument. (2023-04-18)


- It doesn't provide the detailed information during testing or summaryEngineExecutionListener information typically wanted for parameterized integration testing.

---

### Why not use JUnit 4?

JUnit 4 does provide test class level parameterization via `@RunWith(Parameterized.class)`.

- JUnit 4 is maintained, but no new functionality is being added.
c  - The latest release was on February 13, 2021.
  - https://stackoverflow.com/questions/72325172/when-is-junit4-going-to-be-deprecated


- It doesn't provide annotations to run methods before / after all test methods and before / after each test argument.


- It doesn't provide the detailed information during testing or summaryEngineExecutionListener information typically wanted for parameterized integration testing.

---

### What is parameterized integration testing?

Parameterized integration testing is most common when you...

 1. Want to perform integration testing of the application in various environments.


 2. You want to test workflow oriented scenarios of the application.


 3. Various environments could involve different operating systems versions and/or different application runtime versions.

---

### How to lock shared resources?

Use a `Locks` to get a `LockReference` or use `Locks.executeInLock()`

- [examples](/examples/src/test/java/example/locking)

### Why is there no `TestEngine.ResourceLock` annotation?

- Scoping/inheritance is complex


- Annotations don't allow dynamic values

---

### I want to use the AntuBLUE test engine in place of JUnit 5

You *could* use the AntuBLUE test engine in place of Junit 5 for some test scenarios, but that's not the project's goal.


- **It's not a "drop in" replacement**


- **Most JUnit 5 features are missing (by design)**


- **Reference the [Design](/MANUAL.md#design) for the state machine flow**

**Use JUnit5 when appropriate.**

---

### Where is the test extension model/paradigm?

The AntuBLUE test engine does have a global test engine extension model to execute code before and after execution of all tests, but currently, there is no test extension model/paradigm.

- Preliminary work has been done on a test extension mode, but it hasn't been required/requested.


- Any implementation will most likely not be able to use Junit 5 extensions.

