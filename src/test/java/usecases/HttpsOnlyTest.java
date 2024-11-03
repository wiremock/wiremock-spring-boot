package usecases;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

@SpringBootTest
@EnableWireMock({@ConfigureWireMock(port = -1, httpsPort = 0)})
class HttpsOnlyTest {

  @InjectWireMock private WireMockServer wiremock;

  @Value("${wiremock.server.httpsPort}")
  private int wiremockHttpsPort;

  @Value("${wiremock.server.httpsBaseUrl}")
  private String wiremockHttpsUrl;

  @BeforeEach
  public void before() {
    RestAssured.useRelaxedHTTPSValidation();
  }

  @Test
  void testProperties() {
    assertThat(this.wiremockHttpsPort).isNotNull();
    assertThat(this.wiremockHttpsUrl)
        .startsWith("https://")
        .contains(String.valueOf(this.wiremockHttpsPort));
  }

  @Test
  void testInjectedClient() {
    this.wiremock.stubFor(get("/injected-client").willReturn(aResponse().withStatus(202)));

    RestAssured.when().get(this.wiremockHttpsUrl + "/injected-client").then().statusCode(202);

    assertThat(this.wiremock.findAll(anyRequestedFor(anyUrl()))).hasSize(1);
  }

  @Test
  void testDefaultClient() {
    WireMock.stubFor(WireMock.get("/with-default-client").willReturn(aResponse().withStatus(202)));

    RestAssured.when().get(this.wiremockHttpsUrl + "/with-default-client").then().statusCode(202);

    assertThat(WireMock.findAll(anyRequestedFor(anyUrl()))).hasSize(1);
  }
}
