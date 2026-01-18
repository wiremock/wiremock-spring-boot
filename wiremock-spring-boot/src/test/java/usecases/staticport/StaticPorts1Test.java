package usecases.staticport;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

@SpringBootTest
@EnableWireMock({@ConfigureWireMock(port = StaticPorts1Test.STATIC_HTTP_PORT)})
class StaticPorts1Test {
  public static final int STATIC_HTTP_PORT = 8142;

  @Value("${wiremock.server.port}")
  private int wiremockHttpPort;

  @Test
  void testPort() {
    assertThat(this.wiremockHttpPort).isEqualTo(StaticPorts1Test.STATIC_HTTP_PORT);
  }
}
