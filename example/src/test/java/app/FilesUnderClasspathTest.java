package app;

import static org.assertj.core.api.Assertions.assertThat;

import app.todoclient.Todo;
import app.todoclient.TodoClient;
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
      filesUnderClasspath = "custom-location")
})
class FilesUnderClasspathTest {

  @Autowired private TodoClient todoClient;

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
