package usecases;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.restassured.RestAssured;
import io.restassured.config.SSLConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

/**
 * Verifies that {@link ConfigureWireMock#trustStorePath()} and {@link
 * ConfigureWireMock#needClientAuth()} configure the {@link WireMockServer} to require and verify
 * client certificates.
 */
@SpringBootTest
@EnableWireMock({
  @ConfigureWireMock(
      port = -1,
      httpsPort = 0,
      keystorePath = "tls-config/server-keystore.p12",
      keystorePassword = "serverpass",
      keyManagerPassword = "serverpass",
      keystoreType = "PKCS12",
      trustStorePath = "src/test/resources/tls-config/truststore.p12",
      trustStorePassword = "trustpass",
      trustStoreType = "PKCS12",
      needClientAuth = true)
})
class HttpsMutualTlsTest {

  @InjectWireMock private WireMockServer wiremock;

  @BeforeEach
  @AfterEach
  void resetSslConfig() {
    RestAssured.reset();
  }

  @Test
  void acceptsClientsPresentingATrustedCertificate() {
    this.wiremock.stubFor(get("/secure").willReturn(aResponse().withStatus(200)));

    RestAssured.config =
        RestAssured.config()
            .sslConfig(
                SSLConfig.sslConfig()
                    .trustStore("tls-config/truststore.p12", "trustpass")
                    .trustStoreType("PKCS12")
                    .keyStore("tls-config/client-keystore.p12", "clientpass")
                    .keystoreType("PKCS12"));

    RestAssured.given()
        .when()
        .get("https://localhost:" + this.wiremock.httpsPort() + "/secure")
        .then()
        .statusCode(200);
  }

  @Test
  void rejectsClientsThatDoNotPresentACertificate() {
    this.wiremock.stubFor(get("/secure").willReturn(aResponse().withStatus(200)));

    RestAssured.config =
        RestAssured.config()
            .sslConfig(
                SSLConfig.sslConfig()
                    .trustStore("tls-config/truststore.p12", "trustpass")
                    .trustStoreType("PKCS12"));

    assertThatThrownBy(
            () ->
                RestAssured.given()
                    .when()
                    .get("https://localhost:" + this.wiremock.httpsPort() + "/secure"))
        .isNotNull();
  }
}
