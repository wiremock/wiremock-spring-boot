package app;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

public interface UserClient {

  @GetExchange("/{id}")
  User findOne(@PathVariable("id") Long userId);
}
