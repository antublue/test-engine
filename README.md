[![Build](https://github.com/antublue/test-engine/actions/workflows/build.yml/badge.svg)](https://github.com/antublue/test-engine/actions/workflows/build.yml)

<br/>

![AntuBLUE logo](assets/logo.png)

# Test Engine

The AntuBLUE Test Engine is a JUnit 5 based test engine designed specifically for parameterized integration testing by allowing parameterization at the test class level.

# Releases

The Test Engine is published to Maven Central.

**Notes**

Test Engine versions prior to `v4.2.3` should not be used due to a critical bug ...

- [#32](https://github.com/antublue/test-engine/issues/32) Tests may be skipped on a slow machine or scenarios where there are a large number of test classes

# Documentation

**Documentation is specific to a release.**

For the current branch, reference [DOCUMENTATION.md](DOCUMENTATION.md).

For a release, reference the `release-<VERSION>` branch for documentation for the release you are using.

- [v5.0.0](https://github.com/antublue/test-engine/tree/release-5.0.0)
- [v4.3.1](https://github.com/antublue/test-engine/tree/release-4.3.1)
- [v4.3.0](https://github.com/antublue/test-engine/tree/release-4.3.0)

# Getting Help

GitHub's Discussions is the current mechanism for help / support.

# FAQ

- [Fequently Asked Questions](FAQ.md)

# Building

Java 8 or greater is required to build the project. The project targets Java 8 as a baseline.

```shell
git clone https://github.com/antublue/test-engine
cd test-engine
./mvnw clean verify
```

# Contributing

Contributions to the Test Engine are both welcomed and appreciated.

The project uses a [GitHub Flow](https://docs.github.com/en/get-started/quickstart/github-flow) branching strategy, with release branches.

- The [main](/) branch contains the latest unreleased code
- Release branches `release-<VERSION>` contains a release's code and documentation
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