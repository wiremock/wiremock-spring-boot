# WireMock Spring Boot

WireMock Spring Boot drastically simplifies testing HTTP clients in **Spring Boot** & **Junit 5** based integration tests.

> **Warning**
> Project is in development stage, artifacts are not yet published to Maven Central.

## 🤩 Highlights

- fully declarative [WireMock](https://wiremock.org/) setup
- support for multiple `WireMockServer` instances - one per HTTP client as recommended in the WireMock documentation
- automatically sets Spring environment properties
- does not pollute Spring application context with extra beans

## 🤔 How to use

Add the dependency:

```xml
<dependency>
    <groupId>com.maciejwalkowiak</groupId>
    <artifactId>wiremock-spring-boot</artifactId>
    <version>{version}</version>
    <scope>test</scope>
</dependency>
```

Use `@EnableWiremock` with `@ConfigureWiremock` with tests annotated that use `SpringExtension`, like `@SpringBootTest`:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableWiremock({
        @ConfigureWiremock(name = "user-service", property = "user-client.url")
})
class TodoControllerTests {

    @Wiremock("user-service")
    private WireMockServer wiremock;

    @Test
    void aTest() {
        wiremock.stubFor(...);
    }
}
```

- `@EnableWiremock` adds test context customizer and enables `WireMockSpringExtension` 
- `@ConfigureWiremock` creates a `WireMockServer` and passes the `WireMockServer#baseUrl` to a Spring environment property with a name given by `property`.
- `@Wiremock` injects `WireMockServer` instance to a test

Note that `WireMockServer` instances are not added as beans to Spring application context to avoid polluting it with test-related infrastructure. Instead, instances are kept in a separate store associated with an application context.

Sounds good? Consider [❤️ Sponsoring](https://github.com/sponsors/maciejwalkowiak) the project! Thank you!

## 🙏 Credits

I looked into and learned few concepts from following projects and resources during the development of this project: 

- [Spring Cloud Contract WireMock](https://github.com/spring-cloud/spring-cloud-contract/blob/main/spring-cloud-contract-wiremock)
- [Spring Boot WireMock](https://github.com/skuzzle/spring-boot-wiremock)
- [Spring Boot Integration Tests With WireMock and JUnit 5](https://rieckpil.de/spring-boot-integration-tests-with-wiremock-and-junit-5/) by [Philip Riecks](https://twitter.com/rieckpil)
