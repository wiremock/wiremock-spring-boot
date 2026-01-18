package usecases.staticport;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

@SpringBootTest
@EnableWireMock({@ConfigureWireMock(port = StaticPorts1Test.STATIC_HTTP_PORT)})
class StaticPorts2Test {

  @Value("${wiremock.server.port}")
  private int wiremockHttpPort;

  @MockitoBean private ExampleService serviceA;

  @Test
  void testPort() {
    assertThat(this.wiremockHttpPort).isEqualTo(StaticPorts1Test.STATIC_HTTP_PORT);
  }
}
