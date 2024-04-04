package app;

import static org.assertj.core.api.Assertions.assertThat;

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
    @EnableWireMock({
        @ConfigureWireMock(name = "user-service", properties = {"user-service.url", "todo-service.url"}),
        @ConfigureWireMock(name = "mojo-service", property = "mojo-service.url", properties = {"other-service.url"})
    })
    @Nested
    class MultiplePropertiesBindingTest {

        @InjectWireMock("user-service")
        private WireMockServer userServiceWireMockServer;

        @InjectWireMock("mojo-service")
        private WireMockServer mojoServiceWireMockServer;

        @Autowired
        private Environment environment;

        @Test
        void bindsUrlToMultipleProperties() {
            assertThat(environment.getProperty("user-service.url")).isEqualTo(userServiceWireMockServer.baseUrl());
            assertThat(environment.getProperty("todo-service.url")).isEqualTo(userServiceWireMockServer.baseUrl());
            // single property binding takes precedence over multiple properties binding
            assertThat(environment.getProperty("mojo-service.url")).isEqualTo(mojoServiceWireMockServer.baseUrl());
            assertThat(environment.getProperty("other-service.url")).isNull();
        }
    }

    @SpringBootApplication
    static class AppConfiguration {

    }
}
