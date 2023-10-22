package app;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.maciejwalkowiak.wiremock.spring.ConfigureWireMock;
import com.maciejwalkowiak.wiremock.spring.EnableWireMock;
import com.maciejwalkowiak.wiremock.spring.InjectWireMock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = NestedClassWireMockSpringExtensionTest.AppConfiguration.class)
@EnableWireMock({
        @ConfigureWireMock(name = "user-service", property = "user-service.url"),
        @ConfigureWireMock(name = "todo-service", property = "todo-service.url"),
        @ConfigureWireMock(name = "noproperty-service")
})
public class NestedClassWireMockSpringExtensionTest {

    @SpringBootApplication
    static class AppConfiguration {
    }

    @Autowired
    private Environment environment;

    @InjectWireMock("todo-service")
    private WireMockServer topLevelClassTodoService;

    @Nested
    @DisplayName("Test Something")
    class NestedTest {

        @InjectWireMock("todo-service")
        private WireMockServer nestedClassTodoService;

        @Test
        void injectsWiremockServerToMethodParameter(@InjectWireMock("user-service") WireMockServer wireMockServer) {
            assertWireMockServer(wireMockServer, "user-service.url");
        }

        @Test
        void injectsWiremockServerToNestedClassField() {
            assertWireMockServer(nestedClassTodoService, "todo-service.url");
        }

        @Test
        void injectsWiremockServerToTopLevelClassField() {
            assertWireMockServer(topLevelClassTodoService, "todo-service.url");
        }

        @Test
        void doesNotSetPropertyWhenNotProvided(@InjectWireMock("noproperty-service") WireMockServer wireMockServer) {
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
}
