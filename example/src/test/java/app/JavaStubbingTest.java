package app;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static org.assertj.core.api.Assertions.assertThat;

import app.todoclient.TodoClient;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

@SpringBootTest
@EnableWireMock({
  @ConfigureWireMock(
      name = "todo-client",
      baseUrlProperties = "todo-client.url",
      filesUnderClasspath = "custom-location")
})
class JavaStubbingTest {

  @Autowired private TodoClient todoClient;

  @Test
  void usesJavaStubbing() {
    /**
     * If there is only one WireMock configured, you can access WireMock in this static way. If
     * there are several WireMocks configured, you have to use InjectWireMock.
     */
    WireMock.stubFor(
        get("/")
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        """
				[
				    { "id": 1, "userId": 1, "title": "my todo" },
				    { "id": 2, "userId": 1, "title": "my todo2" }
				]
				""")));
    assertThat(this.todoClient.findAll()).isNotNull().hasSize(2);
  }
}
