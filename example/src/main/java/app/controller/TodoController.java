package app.controller;

import app.todoclient.TodoClient;
import app.userclient.UserClient;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TodoController {
  private final UserClient userClient;
  private final TodoClient todoClient;

  public TodoController(final UserClient userClient, final TodoClient todoClient) {
    this.userClient = userClient;
    this.todoClient = todoClient;
  }

  @GetMapping("/")
  public List<TodoDTO> todos() {
    return this.todoClient.findAll().stream()
        .map(
            todo ->
                new TodoDTO(todo.id(), todo.title(), this.userClient.findOne(todo.userId()).name()))
        .toList();
  }
}
