package com.maciejwalkowiak.wiremock.spring;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.junit.Stubbing;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.head;
import static com.github.tomakehurst.wiremock.client.WireMock.headRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.options;
import static com.github.tomakehurst.wiremock.client.WireMock.optionsRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.patchRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.trace;
import static com.github.tomakehurst.wiremock.client.WireMock.traceRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

/**
 * Defines specification for request and response in WireMock based tests.
 *
 * It's a simple abstraction on the top of WireMock request & response spec builders
 * providing simple API and reduces typical for WireMock tests duplication in verification phase.
 */
public class RequestResponseSpec {
    private final ObjectMapper objectMapper;
    private final Stubbing wireMock;
    private String url;
    private RequestMethod method;
    private final Map<String, StringValuePattern> headers = new HashMap<>();
    private Object responseBody;
    private int statusCode = HttpStatus.OK.value();
    private Object requestBody;

    public RequestResponseSpec(ObjectMapper objectMapper, Stubbing wireMock) {
        this.objectMapper = objectMapper;
        this.wireMock = wireMock;
    }

    public RequestResponseSpec method(RequestMethod method) {
        this.method = method;
        return this;
    }

    public RequestResponseSpec url(String url) {
        this.url = url;
        return this;
    }

    public RequestResponseSpec header(String name, String value) {
        return this.header(name, equalTo(value));
    }

    public RequestResponseSpec header(String name, StringValuePattern value) {
        this.headers.put(name, value);
        return this;
    }

    public RequestResponseSpec responseBody(Object responseBody) {
        this.responseBody = responseBody;
        return this;
    }

    public RequestResponseSpec requestBody(Object requestBody) {
        this.header("Content-Type", "application/json");
        this.requestBody = requestBody;
        return this;
    }

    public RequestResponseSpec statusCode(int statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    public RequestResponseSpec response(int statusCode, Object body) {
        return statusCode(statusCode).responseBody(body);
    }

    public RequestResponseSpec stub() {
        try {
            this.wireMock.stubFor(this.mapping());
            return this;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void verify() {
        try {
            this.wireMock.verify(this.requestPattern());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private RequestPatternBuilder requestPattern() throws JsonProcessingException {
        var mapping = switch (method) {
            case GET -> getRequestedFor(urlEqualTo(url));
            case HEAD -> headRequestedFor(urlEqualTo(url));
            case POST -> postRequestedFor(urlEqualTo(url));
            case PUT -> putRequestedFor(urlEqualTo(url));
            case PATCH -> patchRequestedFor(urlEqualTo(url));
            case DELETE -> deleteRequestedFor(urlEqualTo(url));
            case OPTIONS -> optionsRequestedFor(urlEqualTo(url));
            case TRACE -> traceRequestedFor(urlEqualTo(url));
        };
        headers.forEach(mapping::withHeader);
        if (requestBody != null) {
            mapping.withRequestBody(equalToJson(objectMapper.writeValueAsString(requestBody)));
        }
        return mapping;
    }

    private MappingBuilder mapping() throws JsonProcessingException {
        var mapping = switch (method) {
            case GET -> get(urlEqualTo(url));
            case HEAD -> head(urlEqualTo(url));
            case POST -> post(urlEqualTo(url));
            case PUT -> put(urlEqualTo(url));
            case PATCH -> patch(urlEqualTo(url));
            case DELETE -> delete(urlEqualTo(url));
            case OPTIONS -> options(urlEqualTo(url));
            case TRACE -> trace(urlEqualTo(url));
        };
        headers.forEach(mapping::withHeader);
        if (requestBody != null) {
            mapping.withRequestBody(equalToJson(objectMapper.writeValueAsString(requestBody)));
        }
        var responseDefinitionBuilder = aResponse();
        if (responseBody != null) {
            responseDefinitionBuilder.withBody(objectMapper.writeValueAsString(responseBody));
            responseDefinitionBuilder.withHeader("Content-Type", "application/json");
        }
        responseDefinitionBuilder.withStatus(statusCode);

        mapping.willReturn(responseDefinitionBuilder);
        return mapping;
    }
}
