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
@EnableWireMock({
  @ConfigureWireMock(
      name = "wm1",
      portProperties = "wm1.server.port",
      baseUrlProperties = "wm1.server.url",
      filesUnderClasspath = "nomocks"),
  @ConfigureWireMock(
      name = "wm2",
      portProperties = "wm2.server.port",
      baseUrlProperties = "wm2.server.url",
      filesUnderClasspath = "nomocks")
})
class ResetWireMockBetweenTest {

  @InjectWireMock("wm1")
  private WireMockServer wm1;

  @Value("${wm1.server.port}")
  private int wm1Port;

  @Value("${wm1.server.url}")
  private String wm1Url;

  @InjectWireMock("wm2")
  private WireMockServer wm2;

  @Value("${wm2.server.port}")
  private int wm2Port;

  @Value("${wm2.server.url}")
  private String wm2Url;

  private WireMock wm1Client;

  private WireMock wm2Client;

  @BeforeEach
  public void before() {
    this.wm1Client = WireMock.create().port(this.wm1Port).build();
    this.wm1Client.register(
        get("/wm1_configured_in_before").willReturn(aResponse().withStatus(202)));

    this.wm2Client = WireMock.create().port(this.wm2Port).build();
    this.wm2Client.register(
        get("/wm2_configured_in_before").willReturn(aResponse().withStatus(202)));
  }

  @Test
  void test1() {
    assertThat(this.wm1Client.find(anyRequestedFor(anyUrl()))).hasSize(0);
    assertThat(this.wm1Client.allStubMappings().getMappings()).hasSize(1);

    assertThat(this.wm2Client.find(anyRequestedFor(anyUrl()))).hasSize(0);
    assertThat(this.wm2Client.allStubMappings().getMappings()).hasSize(1);

    this.wm1Client.register(get("/wm1_configured_in_test").willReturn(aResponse().withStatus(202)));

    RestAssured.when().get(this.wm1Url + "/wm1_configured_in_test").then().statusCode(202);

    assertThat(this.wm1Client.find(anyRequestedFor(anyUrl()))).hasSize(1);
    assertThat(this.wm1Client.allStubMappings().getMappings()).hasSize(2);

    assertThat(this.wm2Client.find(anyRequestedFor(anyUrl()))).hasSize(0);
    assertThat(this.wm2Client.allStubMappings().getMappings()).hasSize(1);
  }

  @Test
  void test2() {
    assertThat(this.wm1Client.find(anyRequestedFor(anyUrl()))).hasSize(0);
    assertThat(this.wm1Client.allStubMappings().getMappings()).hasSize(1);

    assertThat(this.wm2Client.find(anyRequestedFor(anyUrl()))).hasSize(0);
    assertThat(this.wm2Client.allStubMappings().getMappings()).hasSize(1);

    this.wm2Client.register(get("/wm2_configured_in_test").willReturn(aResponse().withStatus(202)));

    RestAssured.when().get(this.wm2Url + "/wm2_configured_in_test").then().statusCode(202);

    assertThat(this.wm1Client.find(anyRequestedFor(anyUrl()))).hasSize(0);
    assertThat(this.wm1Client.allStubMappings().getMappings()).hasSize(1);

    assertThat(this.wm2Client.find(anyRequestedFor(anyUrl()))).hasSize(1);
    assertThat(this.wm2Client.allStubMappings().getMappings()).hasSize(2);
  }
}
