package app;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.maciejwalkowiak.wiremock.spring.ConfigureWireMock;
import com.maciejwalkowiak.wiremock.spring.EnableWireMock;
import com.maciejwalkowiak.wiremock.spring.InjectWireMock;
import com.maciejwalkowiak.wiremock.spring.RequestResponseSpec;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.bind.annotation.RequestMethod;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static org.assertj.core.api.Assertions.assertThat;

@RestClientTest
@EnableWireMock({
        @ConfigureWireMock(name = "todo-client", property = "todo-client.url")
})
class TodoClientTests {

    @Autowired
    private TodoClient todoClient;

    @Autowired
    private ObjectMapper objectMapper;

    @InjectWireMock("todo-client")
    private WireMockServer wiremock;

    @Test
    void updateTodo() {
        var requestBody = new UpdateTodo("updated todo");

        // define specification for request and response
        var spec = new RequestResponseSpec(objectMapper, wiremock)
                .method(RequestMethod.PUT)
                .url("/todo/5")
                .requestBody(requestBody)
                .response(200, new Todo(5L, 1L, "updated todo"))
                .stub();

        // call the client
        Todo result = todoClient.update(5L, new UpdateTodo("updated todo"));

        // verify if http call matches specs
        spec.verify();

        // assert response body
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(5L);
    }
}
