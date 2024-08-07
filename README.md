[![Build](https://github.com/antublue/test-engine/actions/workflows/build.yml/badge.svg)](https://github.com/antublue/test-engine/actions/workflows/build.yml) [![Codacy Badge](https://app.codacy.com/project/badge/Grade/06036e5d592b46238f98025f297add26)](https://app.codacy.com/gh/antublue/test-engine/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade)

# SUNSET NOTICE

July 19, 2024

Today, we announce the sunset of the AntuBLUE test engine.

While accomplishing the project goals and fully functional, it became apparent that some of the design/API paradigms would require refactoring that would dramatically change usage, resulting in major test changes.

In pursuit of a better paradigm/APIs, we introduce [Verifyica](https://github.com/antublue/verifyica)

# SUNSET NOTICE

<br/>

![AntuBLUE logo](assets/logo.png)

# AntuBLUE Test Engine

The AntuBLUE test engine is a [JUnit 5](https://junit.org/junit5/)-based test engine designed specifically for parameterized container (class) testing.

Common usage scenarios

- integration testing using [Testcontainers](https://java.testcontainers.org/) and [Docker](https://www.docker.com/)
- workflow testing

# Releases

The AntuBLUE test engine is published to Maven Central.

Release information can be found in GitHub Releases

- [Releases](https://github.com/antublue/test-engine/releases)

**Test classes may **require** migration between major releases.**

- [Migration](/MIGRATION.md)

# Documentation

**Documentation is specific to a release.**

For the current branch, reference the manual

- [Manual](MANUAL.md)

For a specific release, reference the `release-<VERSION>` branch for relevant documentation.

# Support

GitHub's Discussions is the current mechanism for help / support.

- [Discussions](https://github.com/antublue/test-engine/discussions)

# FAQ

- [Frequently Asked Questions](FAQ.md)

# Building and Installing

Java 8 or greater is required to build the project. The project targets Java 8 as a baseline.

```shell
git clone https://github.com/antublue/test-engine
cd test-engine
./mvnw clean install
```

# Contributing

Contributions to the AntuBLUE test engine are both welcomed and appreciated.

The project uses a [GitHub Flow](https://docs.github.com/en/get-started/quickstart/github-flow) branching strategy, with release branches for versioned documentation.

Release branch code is considered "locked" - no code changes accepted, but documentation changes are allowed.

- The `main` branch contains the latest unreleased code
- Release branches `release-<VERSION>` contain code and documentation for a specific release
- Google checkstyle format is required
- [Google Java Style](https://google.github.io/styleguide/javaguide.html) is required
- [Spotless](https://github.com/diffplug/spotless) for checking/applying Google Java Style
- PMD is used for static analysis
- Expand all Java imports
- Tags are used for releases

For changes, you should...

- Fork the repository
- Create a branch for your work off of `main`
- Make changes on your branch
- Build and test your changes
- Open a pull request targeting `main`, tagging `@antublue` for review
- A [Developer Certificate of Origin](DCO.md) (DCO) is required for all contributions

# Support

![YourKit logo](https://www.yourkit.com/images/yklogo.png)

[YourKit](https://www.yourkit.com/) supports open source projects with innovative and intelligent tools for monitoring and profiling Java and .NET applications.

YourKit is the creator of <a href="https://www.yourkit.com/java/profiler/">YourKit Java Profiler</a>,
<a href="https://www.yourkit.com/dotnet-profiler/">YourKit .NET Profiler</a>,
and <a href="https://www.yourkit.com/youmonitor/">YourKit YouMonitor</a>.