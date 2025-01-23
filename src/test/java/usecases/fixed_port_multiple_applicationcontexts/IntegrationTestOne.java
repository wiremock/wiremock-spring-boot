package usecases.fixed_port_multiple_applicationcontexts;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

@SpringBootTest
@EnableWireMock({@ConfigureWireMock(name = "service", port = IntegrationTestOne.HTTP_PORT)})
class IntegrationTestOne {
  static final int HTTP_PORT = 8141;

  /**
   * This will trigger a new {@link ApplicationContext}, not same as used in {@link
   * IntegrationTestTwo}.
   */
  @MockitoSpyBean private ServiceA serviceA;

  @Test
  void testWiremock() {
    assertThat(1 + 2).isEqualTo(3);
  }
}
