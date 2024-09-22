package app.todoclient;

import java.util.List;
import org.springframework.web.service.annotation.GetExchange;

public interface TodoClient {
  @GetExchange("/")
  List<Todo> findAll();
}
