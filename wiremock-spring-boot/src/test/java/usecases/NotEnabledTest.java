package usecases;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

@SpringBootTest
class NotEnabledTest {

  @Autowired private Environment env;

  @Test
  void shouldNotHaveWireMockConfigured() {
    assertThrows(
        Exception.class,
        () -> WireMock.stubFor(get("/ping").willReturn(aResponse().withStatus(200))));

    assertThat(this.env.getProperty("wiremock.server.baseUrl")).isNull();
  }
}
