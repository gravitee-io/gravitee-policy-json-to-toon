package io.gravitee.policy.jsontotoon;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import dev.toonformat.jtoon.JToon;
import io.gravitee.apim.gateway.tests.sdk.AbstractPolicyTest;
import io.gravitee.apim.gateway.tests.sdk.annotations.DeployApi;
import io.gravitee.apim.gateway.tests.sdk.annotations.GatewayTest;
import io.gravitee.apim.gateway.tests.sdk.connector.EndpointBuilder;
import io.gravitee.apim.gateway.tests.sdk.connector.EntrypointBuilder;
import io.gravitee.common.http.HttpStatusCode;
import io.gravitee.plugin.endpoint.EndpointConnectorPlugin;
import io.gravitee.plugin.endpoint.http.proxy.HttpProxyEndpointConnectorFactory;
import io.gravitee.plugin.entrypoint.EntrypointConnectorPlugin;
import io.gravitee.plugin.entrypoint.http.proxy.HttpProxyEntrypointConnectorFactory;
import io.gravitee.policy.jsontotoon.configuration.JsonToToonPolicyConfiguration;
import io.vertx.core.http.HttpMethod;
import io.vertx.rxjava3.core.buffer.Buffer;
import io.vertx.rxjava3.core.http.HttpClient;
import java.util.Map;
import org.junit.jupiter.api.Test;

@GatewayTest
public class JsonToToonPolicyHttpProxyApiIntegrationTest extends AbstractPolicyTest<JsonToToonPolicy, JsonToToonPolicyConfiguration> {

    @Override
    public void configureEntrypoints(Map<String, EntrypointConnectorPlugin<?, ?>> entrypoints) {
        entrypoints.putIfAbsent("http-proxy", EntrypointBuilder.build("http-proxy", HttpProxyEntrypointConnectorFactory.class));
    }

    @Override
    public void configureEndpoints(Map<String, EndpointConnectorPlugin<?, ?>> endpoints) {
        endpoints.putIfAbsent("http-proxy", EndpointBuilder.build("http-proxy", (Class) HttpProxyEndpointConnectorFactory.class));
    }

    @Test
    @DeployApi({ "/apis/v4/http-proxy-api-options.json" })
    void should_convert_with_options(HttpClient httpClient) throws Exception {
        var json = """
            {
              "user": {
                "id": 123,
                "name": "Ada",
                "tags": ["reading", "gaming"]
              }
            }""";
        // With PIPE delimiter option configured in the API definition
        var toonWithPipe = """
            user:
              id: 123
              name: Ada
              tags[2|]: reading|gaming""";

        wiremock.stubFor(post("/endpoint").willReturn(ok().withBody(toonWithPipe)));

        httpClient
            .rxRequest(HttpMethod.POST, "/http-proxy-api-options")
            .flatMap(request -> request.rxSend(Buffer.buffer(json)))
            .flatMap(response -> {
                assertThat(response.statusCode()).isEqualTo(HttpStatusCode.OK_200);
                assertThat(response.headers().get("Content-Type")).isEqualTo("application/json");
                return response.body();
            })
            .test()
            .awaitCount(1)
            .assertValue(buffer -> {
                assertThat(buffer.toString()).isEqualTo("{\"user\":{\"id\":123,\"name\":\"Ada\",\"tags\":[\"reading\",\"gaming\"]}}");
                return true;
            })
            .assertNoErrors();

        wiremock.verify(
            1,
            postRequestedFor(urlPathEqualTo("/endpoint"))
                .withRequestBody(equalTo(toonWithPipe))
                .withHeader("Content-Type", equalTo("text/toon"))
        );
    }

    @Test
    @DeployApi({ "/apis/v4/http-proxy-api.json" })
    void should_convert_json_to_toon_and_back_to_json(HttpClient httpClient) throws Exception {
        var json = "{\"hello\":\"world\"}";
        var toon = JToon.encodeJson(json);

        wiremock.stubFor(post("/endpoint").willReturn(ok().withBody(toon)));

        httpClient
            .rxRequest(HttpMethod.POST, "/http-proxy-api")
            .flatMap(request -> request.rxSend(Buffer.buffer(json)))
            .flatMap(response -> {
                assertThat(response.statusCode()).isEqualTo(HttpStatusCode.OK_200);
                assertThat(response.headers().get("Content-Type")).isEqualTo("application/json");
                return response.body();
            })
            .test()
            .awaitCount(1)
            .assertValue(buffer -> {
                assertThat(buffer.toString()).isEqualTo(json);
                return true;
            })
            .assertNoErrors();

        wiremock.verify(1, postRequestedFor(urlPathEqualTo("/endpoint")).withRequestBody(equalTo(toon)));
    }

    @Test
    @DeployApi({ "/apis/v4/http-proxy-api-pretty.json" })
    void should_convert_toon_to_pretty_json(HttpClient httpClient) throws Exception {
        var toon = "hello: world";
        // We look for a multiline JSON output (indicating pretty print)
        // Default ObjectMapper pretty printer uses standard indentation

        wiremock.stubFor(post("/endpoint").willReturn(ok().withBody(toon)));

        httpClient
            .rxRequest(HttpMethod.POST, "/http-proxy-api-pretty")
            .flatMap(request -> request.rxSend(Buffer.buffer(toon)))
            .flatMap(response -> {
                assertThat(response.statusCode()).isEqualTo(HttpStatusCode.OK_200);
                return response.body();
            })
            .test()
            .awaitCount(1)
            .assertValue(buffer -> {
                String json = buffer.toString();
                assertThat(json).isEqualTo(
                    """
                    {
                      "hello" : "world"
                    }"""
                );
                return true;
            })
            .assertNoErrors();

        wiremock.verify(1, postRequestedFor(urlPathEqualTo("/endpoint")).withRequestBody(equalTo(toon)));
    }

    @Test
    @DeployApi({ "/apis/v4/http-proxy-api.json" })
    void should_return_500_when_invalid_json_on_request(HttpClient httpClient) throws Exception {
        var invalidJson = "{\"hello\": \"world\""; // Missing closing brace

        httpClient
            .rxRequest(HttpMethod.POST, "/http-proxy-api")
            .flatMap(request -> request.rxSend(Buffer.buffer(invalidJson)))
            .test()
            .awaitCount(1)
            .assertValue(response -> {
                assertThat(response.statusCode()).isEqualTo(HttpStatusCode.INTERNAL_SERVER_ERROR_500);
                return true;
            })
            .assertNoErrors();

        // Verify that the request never reached the backend
        wiremock.verify(0, postRequestedFor(urlPathEqualTo("/endpoint")));
    }
}
