package corewiremock;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * The service under test in <a
 * href="https://github.com/wiremock/wiremock-spring-boot/issues/210">#210</a>.
 */
@Service
class GetNumberAdapter {
  private final RestClient restClient;

  GetNumberAdapter(final RestClient.Builder builder) {
    this.restClient =
        builder.baseUrl("http://localhost:" + GetNumberAdapterTest.STATIC_HTTP_PORT).build();
  }

  Integer getNumber() {
    return this.restClient.get().uri("/number").retrieve().body(Integer.class);
  }
}
