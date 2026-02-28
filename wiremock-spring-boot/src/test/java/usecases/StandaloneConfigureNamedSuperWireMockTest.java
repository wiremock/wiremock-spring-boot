package usecases;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;

class StandaloneConfigureNamedSuperWireMockTest extends StandaloneConfigureNamedSuperClass {

  @Test
  void wireMockIsStarted() {
    assertThat(userService1).isNotNull();
    assertThat(userServiceUrl1).isNotBlank();
    userService1.stubFor(get("/test1").willReturn(aResponse().withStatus(200)));

    assertThat(userService2).isNotNull();
    assertThat(userServiceUrl2).isNotBlank();
    userService2.stubFor(get("/test2").willReturn(aResponse().withStatus(200)));

    RestAssured.when().get(userServiceUrl1 + "/test1").then().statusCode(200);
    RestAssured.when().get(userServiceUrl2 + "/test2").then().statusCode(200);
  }
}
