package usecases;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

@SpringBootTest
@EnableWireMock({@ConfigureWireMock(port = 8123, staticPortDirtySpringContext = false)})
class StaticPortNoDirtyContextTest {

  @Value("${wiremock.server.port}")
  private int wiremockHttpPort;

  @Test
  void testPort() {
    assertThat(this.wiremockHttpPort).isEqualTo(8123);
  }
}
