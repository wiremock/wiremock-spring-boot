package usecases.cucumber;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class Steps {

  @Qualifier(CucumberConstants.WIREMOCK_SERVER_NAME)
  @Autowired
  private WireMockServer wireMockServer;

  private ExtractableResponse<Response> actualResponse;

  @Before
  public void beforeEach() {
    wireMockServer.resetAll();
    RestAssured.baseURI = "http://localhost:" + wireMockServer.port();
  }

  @Given("^WireMock has endpint (.*)")
  public void wireMockHasEndpoint(String endpoint) {
    StubMapping okResponse =
        WireMock.any(WireMock.urlEqualTo("/" + endpoint)).willReturn(WireMock.status(200)).build();
    wireMockServer.addStubMapping(okResponse);
  }

  @When("^WireMock is invoked with (.*)")
  public void wireMockIsInvokedWith(String endpoint) {
    actualResponse = RestAssured.when().get("/" + endpoint).then().extract();
  }

  @Then("^it should respond (.*)")
  public void isShouldResponsWith(int status) {
    assertThat(actualResponse.statusCode()).isEqualTo(status);
  }
}
