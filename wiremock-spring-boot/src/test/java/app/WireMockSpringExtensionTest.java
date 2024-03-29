package app;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.maciejwalkowiak.wiremock.spring.ConfigureWireMock;
import com.maciejwalkowiak.wiremock.spring.EnableWireMock;
import com.maciejwalkowiak.wiremock.spring.InjectWireMock;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;

public class WireMockSpringExtensionTest {

    @SpringBootTest(classes = WireMockSpringExtensionTest.AppConfiguration.class)
    @EnableWireMock({
            @ConfigureWireMock(name = "user-service", property = "user-service.url"),
            @ConfigureWireMock(name = "todo-service", property = "todo-service.url"),
            @ConfigureWireMock(name = "noproperty-service")
    })
    @Nested
    class SinglePropertyBindingTest {

        @InjectWireMock("todo-service")
        private WireMockServer todoWireMockServer;

        @Autowired
        private Environment environment;

        @Test
        void createsWiremockWithClassLevelConfigureWiremock(@InjectWireMock("user-service") WireMockServer wireMockServer) {
            assertWireMockServer(wireMockServer, "user-service.url");
        }

        @Test
        void createsWiremockWithFieldLevelConfigureWiremock() {
            assertWireMockServer(todoWireMockServer, "todo-service.url");
        }

        @Test
        void doesNotSetPropertyWhenNotProvided(@InjectWireMock("noproperty-service") WireMockServer wireMockServer) {
            assertThat(wireMockServer)
                    .as("inject wiremock sets null when not configured")
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

    @SpringBootTest(classes = WireMockSpringExtensionTest.AppConfiguration.class)
    @EnableWireMock(@ConfigureWireMock(name = "user-service", property = {"user-service.url", "todo-service.url"}))
    @Nested
    class MultiplePropertiesBindingTest {

        @InjectWireMock("user-service")
        private WireMockServer userServiceWireMockServer;

        @Autowired
        private Environment environment;

        @Test
        void bindsUrlToMultipleProperties() {
            assertThat(environment.getProperty("user-service.url")).isEqualTo(userServiceWireMockServer.baseUrl());
            assertThat(environment.getProperty("todo-service.url")).isEqualTo(userServiceWireMockServer.baseUrl());
        }
    }

    @SpringBootApplication
    static class AppConfiguration {

    }
}
