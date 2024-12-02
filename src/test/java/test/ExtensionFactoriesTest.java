package test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.extension.Extension;
import com.github.tomakehurst.wiremock.extension.ExtensionFactory;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformerV2;
import com.github.tomakehurst.wiremock.extension.WireMockServices;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

@SpringBootTest(classes = ExtensionFactoriesTest.AppConfiguration.class)
@EnableWireMock({
  @ConfigureWireMock(
      extensionFactories = {ExtensionFactoriesTest.HeaderAddingExtensionFactory.class})
})
class ExtensionFactoriesTest {

  @InjectWireMock private WireMockServer wm;

  HttpClient httpClient = HttpClient.newHttpClient();

  @Test
  void extensionIsLoaded() throws Exception {
    wm.stubFor(any(anyUrl()).willReturn(ok()));

    HttpResponse<String> response =
        httpClient.send(
            HttpRequest.newBuilder().GET().uri(URI.create(wm.baseUrl() + "/test")).build(),
            HttpResponse.BodyHandlers.ofString());

    assertThat(response.headers().firstValue("added-header").get()).isEqualTo("present");
  }

  @SpringBootApplication
  static class AppConfiguration {}

  public static class HeaderAddingExtensionFactory implements ExtensionFactory {

    @Override
    public List<Extension> create(WireMockServices services) {
      return List.of(new HeaderAddingExtension());
    }
  }

  public static class HeaderAddingExtension implements ResponseDefinitionTransformerV2 {

    @Override
    public ResponseDefinition transform(ServeEvent serveEvent) {
      return ResponseDefinitionBuilder.like(serveEvent.getResponseDefinition())
          .withHeader("added-header", "present")
          .build();
    }

    @Override
    public boolean applyGlobally() {
      return true;
    }

    @Override
    public String getName() {
      return "header-add";
    }
  }
}
