package usecases;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "usecases")
public class App {

  public static void main(final String[] args) {
    SpringApplication.run(App.class, args);
  }
}
