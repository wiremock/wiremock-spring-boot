# WireMock Spring Boot

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.wiremock.integrations/wiremock-spring-boot/badge.svg)](https://search.maven.org/artifact/org.wiremock.integrations/wiremock-spring-boot)

**WireMock Spring Boot** library drastically simplifies WireMock configuration in a **Spring Boot** and **JUnit 5** application.

See the [WireMock Spring Boot doc page](https://wiremock.org/docs/spring-boot/) for installation and usage details. There are also [running examples of use cases](https://github.com/wiremock/wiremock-spring-boot/tree/main/src/test/java/usecases).

## Highlights

* Fully declarative [WireMock](https://wiremock.org/) setup
* Support for **multiple** `WireMockServer` **instances** - one per HTTP client as recommended in the WireMock documentation
* Automatically sets Spring environment properties
* Does not pollute Spring application context with extra beans


## Credits

* [Maciej Walkowiak](https://github.com/maciejwalkowiak) - This was originally his project and later moved to WireMock organization
* [Spring Cloud Contract WireMock](https://github.com/spring-cloud/spring-cloud-contract/blob/main/spring-cloud-contract-wiremock)
* [Spring Boot WireMock](https://github.com/skuzzle/spring-boot-wiremock)
* [Spring Boot Integration Tests With WireMock and JUnit 5](https://rieckpil.de/spring-boot-integration-tests-with-wiremock-and-junit-5/) by [Philip Riecks](https://twitter.com/rieckpil)

Originally forked from [WireMock Spring Boot](https://github.com/maciejwalkowiak/wiremock-spring-boot).
