package app;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.maciejwalkowiak.wiremock.spring.ConfigureWiremock;
import com.maciejwalkowiak.wiremock.spring.EnableWiremock;
import com.maciejwalkowiak.wiremock.spring.Wiremock;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnableWiremock({
        @ConfigureWiremock(name = "todo-client", property = "todo-client.url")
})
class TodoClientTests {

    @Autowired
    private TodoClient todoClient;

    @Wiremock("todo-client")
    private WireMockServer wiremock;

    @Test
    void findsTodos() {
        wiremock.stubFor(get("/").willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("""
                        [
                            { "id": 1, "userId": 1, "title": "my todo" },
                            { "id": 2, "userId": 1, "title": "my todo2" }
                        ]
                        """)));
        assertThat(todoClient.findAll()).isNotNull().hasSize(2);
    }

}
