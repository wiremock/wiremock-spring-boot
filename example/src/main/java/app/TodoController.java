package app;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TodoController {
    private final UserClient userClient;
    private final TodoClient todoClient;

    public TodoController(UserClient userClient, TodoClient todoClient) {
        this.userClient = userClient;
        this.todoClient = todoClient;
    }

    @GetMapping("/")
    List<TodoDTO> todos() {
        return todoClient.findAll()
                .stream()
                .map(todo -> new TodoDTO(todo.id(), todo.title(), userClient.findOne(todo.userId()).name()))
                .toList();
    }

    record TodoDTO(Long id, String title, String userName) {
    }
}
