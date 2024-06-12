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

Use a `Store`

---

### I want to use the Test Engine in place of JUnit 5

You can use the test engine in place of Junit 5 in somes scenarios where you don't really have parameterized test classes or need to mix testing of parameterized classes and standard classes 

- **It's not a "drop in" replacement.**
- **Most JUnit 5 features are missing (by design.)**
- **Reference the [Design](/MANUAL.md#design) for the state machine flow.**

Example:

- [Junit5LikeTest.java](/examples/src/test/java/example/Junit5LikeTest.java)

---

### Where is the `Extension` model/paradigm?

Currently, there is no `Extension` model/paradigm.

Preliminary work has been done, but it hasn't been required/requested.

Any implementation will most likely not be able to use Junit 5 extensions.

