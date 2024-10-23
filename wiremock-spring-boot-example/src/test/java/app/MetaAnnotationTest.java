package app;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.client.WireMock;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

@MetaAnnotation
class MetaAnnotationTest {

  @Value("${customUrl}")
  private String customUrl;

  @Value("${sameCustomUrl}")
  private String sameCustomUrl;

  @Value("${customPort}")
  private String customPort;

  @BeforeEach
  public void before() {
    WireMock.stubFor(get("/the_custom_prop_mock").willReturn(aResponse().withStatus(202)));
  }

  @Test
  void test() {
    assertThat(this.customUrl).isEqualTo(this.sameCustomUrl);

    RestAssured.baseURI = this.customUrl;
    RestAssured.when().get("/the_custom_prop_mock").then().statusCode(202);
  }
}
