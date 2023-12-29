package app;

import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

public interface TodoClient {
    @GetExchange("/")
    List<Todo> findAll();

    @PutExchange("/todo/{id}")
    Todo update(@PathVariable("id") Long id, @RequestBody UpdateTodo update);
}
