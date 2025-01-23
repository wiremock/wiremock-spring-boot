package usecases.fixed_port_multiple_applicationcontexts;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

@SpringBootTest
@EnableWireMock({@ConfigureWireMock(name = "service", port = IntegrationTestOne.HTTP_PORT)})
class IntegrationTestTwo {

  @Test
  void testWiremock() {
    assertThat(3 + 1).isEqualTo(4);
  }
}
