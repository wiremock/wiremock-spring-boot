# WireMock Spring Boot

**WireMock Spring Boot** library drastically simplifies [WireMock](https://wiremock.org) configuration in a **Spring Boot** and **JUnit 5** application.

## 🤩 Highlights

- fully declarative [WireMock](https://wiremock.org/) setup
- support for **multiple** `WireMockServer` **instances** - one per HTTP client as recommended in the WireMock documentation
- automatically sets Spring environment properties
- does not pollute Spring application context with extra beans

## 🤔 How to install

Add the dependency to `wiremock-spring-boot`:

```xml
<dependency>
    <groupId>com.maciejwalkowiak.spring</groupId>
    <artifactId>wiremock-spring-boot</artifactId>
    <version>1.0.0</version>
    <scope>test</scope>
</dependency>
```

## ✨ How to use

Use `@EnableWireMock` with `@ConfigureWireMock` with tests annotated that use `SpringExtension`, like `@SpringBootTest`:

```java
@SpringBootTest
@EnableWireMock({
        @ConfigureWireMock(name = "user-service", property = "user-client.url")
})
class TodoControllerTests {

    @WireMock("user-service")
    private WireMockServer wiremock;
    
    @Autowired
    private Environment env;

    @Test
    void aTest() {
        env.getProperty("user-client.url"); // returns a URL to WireMockServer instance
        wiremock.stubFor(...);
    }
}
```

- `@EnableWireMock` adds test context customizer and enables `WireMockSpringExtension` 
- `@ConfigureWireMock` creates a `WireMockServer` and passes the `WireMockServer#baseUrl` to a Spring environment property with a name given by `property`.
- `@WireMock` injects `WireMockServer` instance to a test

Note that `WireMockServer` instances are not added as beans to Spring application context to avoid polluting it with test-related infrastructure. Instead, instances are kept in a separate store associated with an application context.

### Registering WireMock extensions

WireMock extensions can be registered independently with each `@ConfigureWireMock`:

```java
@ConfigureWireMock(name = "...", property = "...", extensions = { ... })
```

### Customizing mappings directory

By default, each `WireMockServer` is configured to load mapping files from a classpath directory `wiremock/{server-name}/mappings`.

It can be changed with setting `stubLocation` on `@ConfigureWireMock`:

```java
@ConfigureWireMock(name = "...", property = "...", stubLocation = "my-stubs")
```

Sounds good? Consider [❤️ Sponsoring](https://github.com/sponsors/maciejwalkowiak) the project! Thank you!

## 🙏 Credits

I looked into and learned few concepts from following projects and resources during the development of this project: 

- [Spring Cloud Contract WireMock](https://github.com/spring-cloud/spring-cloud-contract/blob/main/spring-cloud-contract-wiremock)
- [Spring Boot WireMock](https://github.com/skuzzle/spring-boot-wiremock)
- [Spring Boot Integration Tests With WireMock and JUnit 5](https://rieckpil.de/spring-boot-integration-tests-with-wiremock-and-junit-5/) by [Philip Riecks](https://twitter.com/rieckpil)
