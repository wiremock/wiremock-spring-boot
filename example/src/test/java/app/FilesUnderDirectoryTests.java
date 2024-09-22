package app;

import static org.assertj.core.api.Assertions.assertThat;

import app.userclient.User;
import app.userclient.UserClient;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

@SpringBootTest
@EnableWireMock({
  @ConfigureWireMock(
      name = "user-client",
      baseUrlProperties = "user-client.url",
      filesUnderDirectory = "src/test/wiremock-mappings/user-client")
})
class FilesUnderDirectoryTests {

  @Autowired private UserClient userClient;

  @InjectWireMock("user-client")
  private WireMockServer wiremock;

  @Test
  void usesStubFiles() {
    final User user = this.userClient.findOne(1L);
    assertThat(user).isNotNull();
  }
}
