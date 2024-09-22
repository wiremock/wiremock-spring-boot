package app;

import java.util.List;
import org.springframework.web.service.annotation.GetExchange;

public interface TodoClient {
  @GetExchange("/")
  List<Todo> findAll();
}
