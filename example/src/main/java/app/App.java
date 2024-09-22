package app;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@SpringBootApplication
public class App {

  public static void main(String[] args) {
    SpringApplication.run(App.class, args);
  }

  @Bean
  UserClient userClient(WebClient.Builder builder, @Value("${user-client.url}") String url) {
    WebClient webClient = builder.baseUrl(url).build();

    HttpServiceProxyFactory httpServiceProxyFactory =
        HttpServiceProxyFactory.builder(WebClientAdapter.forClient(webClient)).build();
    return httpServiceProxyFactory.createClient(UserClient.class);
  }

  @Bean
  TodoClient todoClient(WebClient.Builder builder, @Value("${todo-client.url}") String url)
      throws InterruptedException {
    Thread.sleep(1000);
    WebClient webClient = builder.baseUrl(url).build();

    HttpServiceProxyFactory httpServiceProxyFactory =
        HttpServiceProxyFactory.builder(WebClientAdapter.forClient(webClient)).build();
    return httpServiceProxyFactory.createClient(TodoClient.class);
  }
}
