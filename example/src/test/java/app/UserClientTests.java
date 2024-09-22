package app;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

@SpringBootTest
@EnableWireMock({@ConfigureWireMock(name = "user-client", property = "user-client.url")})
class UserClientTests {

  @Autowired private UserClient userClient;

  @InjectWireMock("user-client")
  private WireMockServer wiremock;

  @Test
  void usesJavaStubbing() {
    wiremock.stubFor(
        get("/2")
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        """
                        { "id": 2, "name": "Amy" }
                        """)));
    User user = userClient.findOne(2L);
    assertThat(user).isNotNull();
    assertThat(user.id()).isEqualTo(2L);
    assertThat(user.name()).isEqualTo("Amy");
  }

  @Test
  void usesStubFiles() {
    User user = userClient.findOne(1L);
    assertThat(user).isNotNull();
    assertThat(user.id()).isEqualTo(1L);
    assertThat(user.name()).isEqualTo("Jenna");
  }
}
