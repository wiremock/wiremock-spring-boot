package app;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.maciejwalkowiak.wiremock.spring.ConfigureWiremock;
import com.maciejwalkowiak.wiremock.spring.EnableWiremock;
import com.maciejwalkowiak.wiremock.spring.Wiremock;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableWiremock({
        @ConfigureWiremock(name = "user-service", property = "user-client.url"),
        @ConfigureWiremock(name = "todo-service", property = "todo-client.url")
})
class TodoControllerTests {

    @Wiremock("todo-service")
    private WireMockServer todoService;

    @Wiremock("user-service")
    private WireMockServer userService;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void returnsTodos() {
        todoService.stubFor(get("/").willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("""
                        [
                            { "id": 1, "userId": 1, "title": "my todo" },
                            { "id": 2, "userId": 2, "title": "my todo2" }
                        ]
                        """)));

        userService.stubFor(get("/1").willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("""
                        { "id": 1, "name": "Amy" }
                        """)));

        userService.stubFor(get("/2").willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("""
                        { "id": 2, "name": "John" }
                        """)));

        ResponseEntity<TodoController.TodoDTO[]> response = restTemplate.getForEntity("/", TodoController.TodoDTO[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody()).hasSize(2).satisfies(todos -> {
            assertThat(todos[0].id()).isEqualTo(1);
            assertThat(todos[0].title()).isEqualTo("my todo");
            assertThat(todos[0].userName()).isEqualTo("Amy");

            assertThat(todos[1].id()).isEqualTo(2);
            assertThat(todos[1].title()).isEqualTo("my todo2");
            assertThat(todos[1].userName()).isEqualTo("John");
        });
    }
}
