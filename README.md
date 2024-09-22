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
    <groupId>org.wiremock.spring</groupId>
    <artifactId>wiremock-spring-boot</artifactId>
    <version>X</version>
    <scope>test</scope>
</dependency>
```

```groovy
testImplementation "org.wiremock.spring:wiremock-spring-boot:X"
```

## ✨ How to use

Use `@EnableWireMock` with `@ConfigureWireMock` with tests annotated that use `SpringExtension`, like `@SpringBootTest`:

```java
@SpringBootTest
@EnableWireMock({
        @ConfigureWireMock(name = "user-service",
        baseUrlProperties = "user-service.url",
        portProperties = "user-service.port")
})
class TodoControllerTests {

    @InjectWireMock("user-service")
    private WireMockServer wiremock;

    @Value("${user-service.url}")
    private String wiremockUrl;

    @Value("${user-service.port}")
    private String wiremockPort;

    @Test
    void aTest() {
        wiremock.stubFor(...);
    }
}
```

- `@EnableWireMock` adds test context customizer and enables `WireMockSpringExtension` 
- `@ConfigureWireMock` creates a `WireMockServer` and passes the `WireMockServer#baseUrl` to a Spring environment property with a name given by `baseUrlProperties`.
- `@InjectWireMock` injects `WireMockServer` instance to a test

Note that `WireMockServer` instances are not added as beans to Spring application context to avoid polluting it with test-related infrastructure. Instead, instances are kept in a separate store associated with an application context.

### Registering WireMock extensions

WireMock extensions can be registered independently with each `@ConfigureWireMock`:

```java
@ConfigureWireMock(name = "...", baseUrlProperties = "...", extensions = { ... })
```

### Single vs Multiple Property Injection

The concept of single property injection can be described as wiring _one_ `WireMockServer` instance to _one_ property.

```java
@SpringBootTest
@EnableWireMock({
    @ConfigureWireMock(name = "foo-service", baseUrlProperties = "app.client-apis.foo.base-path"}),
    @ConfigureWireMock(name = "bar-service", baseUrlProperties = "app.client-apis.bar.base-path"}),
    @ConfigureWireMock(name = "mojo-service", baseUrlProperties = "app.client-apis.mojo.base-path"})
})
class AppIT { 
    @InjectWireMock("foo-service")
    private WireMockServer fooService;
    @InjectWireMock("bar-service")
    private WireMockServer barService;
    @InjectWireMock("mojo-service")
    private WireMockServer mojoService;
    
    @Test
    void contextLoads() {
        // your test code
    }
}
```

The concept of multiple property injection can be described as wiring _one_ `WireMockServer` instance to _multiple_ properties.

```java
@SpringBootTest
@EnableWireMock({
    @ConfigureWireMock(name = "services", baseUrlProperties = {
        "app.client-apis.foo.base-path",
        "app.client-apis.bar.base-path",
        "app.client-apis.mojo.base-path"})
})
class AppIT {

    @InjectWireMock("services")
    private WireMockServer services;

    @Test
    void contextLoads() {
        // your test code
    }
}
```

The *single* property injection provides a high level of isolation when mocking and stubbing 3rd pary RESTful api, because every service 
is associated to its own dedicated `WireMockServer` instance.
The *multiple* property injections provides a less complex test setup at the cost of isolation.

### Customizing mappings directory

By default, each `WireMockServer` is configured to load mapping files from a classpath directory `wiremock/{server-name}/mappings`.

It can be changed with setting `stubLocation` on `@ConfigureWireMock`:

```java
@ConfigureWireMock(name = "...", baseUrlProperties = "...", stubLocation = "my-stubs")
```

## 🙏 Credits

- [Wiremock Spring Boot](https://github.com/maciejwalkowiak/wiremock-spring-boot) (this is based on that project!)
- [Spring Cloud Contract WireMock](https://github.com/spring-cloud/spring-cloud-contract/blob/main/spring-cloud-contract-wiremock)
- [Spring Boot WireMock](https://github.com/skuzzle/spring-boot-wiremock)
- [Spring Boot Integration Tests With WireMock and JUnit 5](https://rieckpil.de/spring-boot-integration-tests-with-wiremock-and-junit-5/) by [Philip Riecks](https://twitter.com/rieckpil)
