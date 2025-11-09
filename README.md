# WireMock Spring Boot

[![Maven Central](https://img.shields.io/maven-central/v/org.wiremock.integrations/wiremock-spring-boot.svg?label=wiremock-spring-boot)](https://central.sonatype.com/artifact/org.wiremock.integrations/wiremock-spring-boot) [![Maven Central](https://img.shields.io/maven-central/v/org.wiremock.integrations/wiremock-spring-boot.svg?label=wiremock-spring-boot-standalone)](https://central.sonatype.com/artifact/org.wiremock.integrations/wiremock-spring-boot-standalone)

---

<table>
<tr>
<td>
<img src="https://wiremock.org/images/wiremock-cloud/wiremock_cloud_logo.png" alt="WireMock Cloud Logo" height="20" align="left">
<strong>WireMock open source is supported by <a href="https://www.wiremock.io/cloud-overview?utm_source=github.com&utm_campaign=wiremock-spring-boot-README.md-banner">WireMock Cloud</a>. Please consider trying it out if your team needs advanced capabilities such as OpenAPI, dynamic state, data sources and more.</strong>
</td>
</tr>
</table>

---

**WireMock Spring Boot** library drastically simplifies WireMock configuration in a **Spring Boot** and **JUnit** application.

See the [WireMock Spring Boot doc page](https://wiremock.org/docs/spring-boot/) for installation and usage details. There are also [running examples of use cases](https://github.com/wiremock/wiremock-spring-boot/tree/main/wiremock-spring-boot/src/test/java/usecases).

## Highlights

* Fully declarative [WireMock](https://wiremock.org/) setup
* Support for **multiple** `WireMockServer` **instances** - one per HTTP client as recommended in the WireMock documentation
* Automatically sets Spring environment properties
* Does not pollute Spring application context with extra beans
* Available in `org.wiremock.integrations:wiremock-spring-boot` and standalone without transitives in `org.wiremock.integrations:wiremock-spring-boot-standalone`

## Credits

* [Maciej Walkowiak](https://github.com/maciejwalkowiak) - This was originally his project and later moved to WireMock organization
* [Spring Cloud Contract WireMock](https://github.com/spring-cloud/spring-cloud-contract/blob/main/spring-cloud-contract-wiremock)
* [Spring Boot WireMock](https://github.com/skuzzle/spring-boot-wiremock)
* [Spring Boot Integration Tests With WireMock and JUnit 5](https://rieckpil.de/spring-boot-integration-tests-with-wiremock-and-junit-5/) by [Philip Riecks](https://twitter.com/rieckpil)

Originally forked from [WireMock Spring Boot](https://github.com/maciejwalkowiak/wiremock-spring-boot).
