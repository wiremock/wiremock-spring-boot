package app;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static org.assertj.core.api.Assertions.assertThat;

import app.todoclient.Todo;
import app.todoclient.TodoClient;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

@SpringBootTest
@EnableWireMock({
  @ConfigureWireMock(
      name = "todo-client",
      baseUrlProperties = "todo-client.url",
      stubLocation = "custom-location")
})
class TodoClientTests {

  @Autowired private TodoClient todoClient;

  @Test
  void usesJavaStubbing() {
    /**
     * If there is only one WireMock configured, you can access WireMock in this static way. If
     * there are several WireMocks configured, you have to use InjectWireMock.
     */
    WireMock.stubFor(
        get("/")
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        """
				[
				    { "id": 1, "userId": 1, "title": "my todo" },
				    { "id": 2, "userId": 1, "title": "my todo2" }
				]
				""")));
    assertThat(this.todoClient.findAll()).isNotNull().hasSize(2);
  }

  @Test
  void usesStubFilesFromCustomLocation() {
    final List<Todo> todos = this.todoClient.findAll();
    assertThat(todos).isNotNull().hasSize(2);
    assertThat(todos.get(0))
        .satisfies(
            todo -> {
              assertThat(todo.id()).isEqualTo(1);
              assertThat(todo.title()).isEqualTo("custom location todo 1");
            });
    assertThat(todos.get(1))
        .satisfies(
            todo -> {
              assertThat(todo.id()).isEqualTo(2);
              assertThat(todo.title()).isEqualTo("custom location todo 2");
            });
  }
}
