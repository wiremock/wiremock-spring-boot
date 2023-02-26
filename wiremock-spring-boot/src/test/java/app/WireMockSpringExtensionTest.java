package app;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.maciejwalkowiak.wiremock.spring.ConfigureWiremock;
import com.maciejwalkowiak.wiremock.spring.EnableWiremock;
import com.maciejwalkowiak.wiremock.spring.Wiremock;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = WireMockSpringExtensionTest.AppConfiguration.class)
@EnableWiremock({
        @ConfigureWiremock(name = "user-service", property = "user-service.url"),
        @ConfigureWiremock(name = "todo-service", property = "todo-service.url"),
        @ConfigureWiremock(name = "noproperty-service")
})
public class WireMockSpringExtensionTest {

    @SpringBootApplication
    static class AppConfiguration {

    }

    @Wiremock("todo-service")
    private WireMockServer todoWireMockServer;

    @Autowired
    private Environment environment;

    @Test
    void createsWiremockWithClassLevelConfigureWiremock(@Wiremock("user-service") WireMockServer wireMockServer) {
        assertWireMockServer(wireMockServer, "user-service.url");
    }

    @Test
    void createsWiremockWithFieldLevelConfigureWiremock() {
        assertWireMockServer(todoWireMockServer, "todo-service.url");
    }

    @Test
    void doesNotSetPropertyWhenNotProvided(@Wiremock("noproperty-service") WireMockServer wireMockServer) {
        assertThat(wireMockServer)
                .as("creates WireMock instance")
                .isNotNull();
    }

    private void assertWireMockServer(WireMockServer wireMockServer, String property) {
        assertThat(wireMockServer)
                .as("creates WireMock instance")
                .isNotNull();
        assertThat(wireMockServer.baseUrl())
                .as("WireMock baseUrl is set")
                .isNotNull();
        assertThat(wireMockServer.port())
                .as("sets random port")
                .isNotZero();
        assertThat(environment.getProperty(property))
                .as("sets Spring property")
                .isEqualTo(wireMockServer.baseUrl());
    }
}
