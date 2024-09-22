package app;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.wiremock.wiremock.spring.ConfigureWireMock;
import org.wiremock.wiremock.spring.EnableWireMock;
import org.wiremock.wiremock.spring.InjectWireMock;

@SpringBootTest
@EnableWireMock({
  @ConfigureWireMock(
      name = "user-client",
      property = "user-client.url",
      stubLocation = "src/test/wiremock-mappings/user-client",
      stubLocationOnClasspath = false)
})
class StubsInDirectoryTests {

  @Autowired private UserClient userClient;

  @InjectWireMock("user-client")
  private WireMockServer wiremock;

  @Test
  void usesStubFiles() {
    User user = userClient.findOne(1L);
    assertThat(user).isNotNull();
  }
}
