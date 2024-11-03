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

@SpringBootTest(properties = {"wm1.server.port=65432", "wm2.server.port=54321"})
@EnableWireMock({
  @ConfigureWireMock(
      name = "wm1",
      portProperties = "wm1.server.port",
      baseUrlProperties = "wm1.server.url",
      usePortFromPredefinedPropertyIfFound = true),
  @ConfigureWireMock(name = "wm2", portProperties = "wm2.server.port")
})
class UsePortFromPropertyTest {

  @InjectWireMock("wm1")
  private WireMockServer wm1;

  @Value("${wm1.server.port}")
  private int wm1Port;

  @Value("${wm1.server.url}")
  private String wm1Url;

  private WireMock wm1Client;

  @Value("${wm2.server.port}")
  private int wm2Port;

  @BeforeEach
  public void before() {
    this.wm1Client = WireMock.create().port(this.wm1Port).build();
  }

  @Test
  void testThatPortCanBePredefinedInPropertyAndOptionallyDisabled() {
    assertThat(this.wm1Port).isEqualTo(65432);
    assertThat(this.wm1Url).contains(String.valueOf(this.wm1Port));

    assertThat(this.wm2Port).isNotEqualTo(54321).isNotEqualTo(this.wm1Port);

    this.wm1Client.register(get("/wm1_configured_in_test").willReturn(aResponse().withStatus(202)));

    RestAssured.when().get(this.wm1Url + "/wm1_configured_in_test").then().statusCode(202);

    assertThat(this.wm1Client.find(anyRequestedFor(anyUrl()))).hasSize(1);
  }
}
