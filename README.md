[![Build](https://github.com/antublue/test-engine/actions/workflows/build.yml/badge.svg)](https://github.com/antublue/test-engine/actions/workflows/build.yml)

<br/>

![AntuBLUE logo](documentation/assets/logo.png)

# Test Engine

The AntuBLUE Test Engine is a JUnit 5 based test engine designed specifically for parameterized integration testing by allowing parameterization at the test class level.

# Releases

The Test Engine is published to Maven Central.

**Notes**

Test Engine versions prior to `v4.2.3` should not be used due to a critical bug ...

- [#32](https://github.com/antublue/test-engine/issues/32) Tests may be skipped on a slow machine or scenarios where there are a large number of test classes

# Documentation

Documentation is specific to a release.

- [v5.0.0](documentation/v5.0.0/README.md)
- [v4.3.1](documentation/v4.3.1/README.md)

# Building

Java 8 or greater is required to build the project. The project targets Java 8 as a baseline.

```shell
git clone https://github.com/antublue/test-engine
cd test-engine
./mvnw clean verify
```

# Contributing

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
- [Developer Certificate of Origin](https://github.com/apps/dco) (DCO) is required for all contributions

# Getting Help

GitHub's Discussions is the current mechanism for help / support.
