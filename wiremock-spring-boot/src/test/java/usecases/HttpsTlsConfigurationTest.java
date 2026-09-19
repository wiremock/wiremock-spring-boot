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
 * Verifies that {@link ConfigureWireMock#keystorePath()} and friends configure the {@link
 * WireMockServer} to serve HTTPS with a custom keystore, resolved from the test classpath, instead
 * of WireMock's bundled self-signed certificate.
 */
@SpringBootTest
@EnableWireMock({
  @ConfigureWireMock(
      port = -1,
      httpsPort = 0,
      keystorePath = "tls-config/server-keystore.p12",
      keystorePassword = "serverpass",
      keyManagerPassword = "serverpass",
      keystoreType = "PKCS12")
})
class HttpsTlsConfigurationTest {

  @InjectWireMock private WireMockServer wiremock;

  @BeforeEach
  @AfterEach
  void resetSslConfig() {
    RestAssured.reset();
  }

  @Test
  void servesHttpsWithConfiguredKeystore() {
    this.wiremock.stubFor(get("/secure").willReturn(aResponse().withStatus(200)));

    // A client that only trusts our custom "server" certificate (not WireMock's default
    // self-signed one) must be able to complete the handshake and call the server. If the
    // annotation's keystore settings were ignored, this would fail with an SSLHandshakeException
    // because the trust store below does not contain WireMock's bundled certificate.
    RestAssured.config =
        RestAssured.config()
            .sslConfig(
                SSLConfig.sslConfig()
                    .trustStore("tls-config/truststore.p12", "trustpass")
                    .trustStoreType("PKCS12"));

    RestAssured.given()
        .when()
        .get("https://localhost:" + this.wiremock.httpsPort() + "/secure")
        .then()
        .statusCode(200);
  }

  @Test
  void rejectsClientsThatDoNotTrustTheConfiguredKeystore() {
    this.wiremock.stubFor(get("/secure").willReturn(aResponse().withStatus(200)));

    // The JVM's default trust store does not trust our self-signed certificate, proving the
    // server is not falling back to some already-trusted default.
    assertThatThrownBy(
            () ->
                RestAssured.given()
                    .when()
                    .get("https://localhost:" + this.wiremock.httpsPort() + "/secure"))
        .hasMessageContaining("PKIX path");
  }
}
