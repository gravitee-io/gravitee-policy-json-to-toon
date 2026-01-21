/*
 * Copyright © 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.policy.jsontotoon;

import static org.assertj.core.api.Assertions.assertThat;

import com.graviteesource.entrypoint.http.get.HttpGetEntrypointConnectorFactory;
import com.graviteesource.reactor.message.MessageApiReactorFactory;
import io.gravitee.apim.gateway.tests.sdk.AbstractPolicyTest;
import io.gravitee.apim.gateway.tests.sdk.annotations.DeployApi;
import io.gravitee.apim.gateway.tests.sdk.annotations.GatewayTest;
import io.gravitee.apim.gateway.tests.sdk.connector.EndpointBuilder;
import io.gravitee.apim.gateway.tests.sdk.connector.EntrypointBuilder;
import io.gravitee.apim.gateway.tests.sdk.reactor.ReactorBuilder;
import io.gravitee.apim.plugin.reactor.ReactorPlugin;
import io.gravitee.common.http.HttpStatusCode;
import io.gravitee.common.http.MediaType;
import io.gravitee.gateway.api.http.HttpHeaderNames;
import io.gravitee.gateway.reactive.reactor.v4.reactor.ReactorFactory;
import io.gravitee.plugin.endpoint.EndpointConnectorPlugin;
import io.gravitee.plugin.endpoint.mock.MockEndpointConnectorFactory;
import io.gravitee.plugin.entrypoint.EntrypointConnectorPlugin;
import io.gravitee.policy.jsontotoon.configuration.JsonToToonPolicyConfiguration;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.buffer.Buffer;
import io.vertx.rxjava3.core.http.HttpClient;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

@GatewayTest
public class JsonToToonPolicyMessageApiIntegrationTest extends AbstractPolicyTest<JsonToToonPolicy, JsonToToonPolicyConfiguration> {

    @Override
    public void configureReactors(Set<ReactorPlugin<? extends ReactorFactory<?>>> reactors) {
        reactors.add(ReactorBuilder.build(MessageApiReactorFactory.class));
    }

    @Override
    public void configureEntrypoints(Map<String, EntrypointConnectorPlugin<?, ?>> entrypoints) {
        entrypoints.putIfAbsent("http-get", EntrypointBuilder.build("http-get", HttpGetEntrypointConnectorFactory.class));
    }

    @Override
    public void configureEndpoints(Map<String, EndpointConnectorPlugin<?, ?>> endpoints) {
        endpoints.putIfAbsent("mock", EndpointBuilder.build("mock", MockEndpointConnectorFactory.class));
    }

    @Test
    @DeployApi({ "/apis/v4/message-api.json" })
    void should_convert_json_to_toon_on_message_request(HttpClient httpClient) throws Exception {
        httpClient
            .rxRequest(HttpMethod.GET, "/message-api")
            .flatMap(request -> request.putHeader(HttpHeaderNames.ACCEPT, MediaType.APPLICATION_JSON).rxSend())
            .flatMap(response -> {
                assertThat(response.statusCode()).isEqualTo(HttpStatusCode.OK_200);
                assertThat(response.headers().get("Content-Type")).isEqualTo("application/json");
                return response.body();
            })
            .map(Buffer::toString)
            .test()
            .awaitCount(1)
            .assertValue(body -> {
                var toonExpected = """
                    user:
                      id: 123
                      name: Ada
                      tags[2]: reading,gaming""";

                final JsonObject content = new JsonObject(body);
                final JsonArray items = content.getJsonArray("items");
                assertThat(items).hasSize(2);
                items.forEach(item -> {
                    JsonObject message = (JsonObject) item;
                    assertThat(message.getString("content")).isEqualTo(toonExpected);
                    final JsonObject headers = message.getJsonObject("headers");
                    assertThat(headers.getJsonArray("Content-Type")).hasSize(1).contains("text/toon");
                    assertThat(headers.getJsonArray("Content-Length")).hasSize(1).contains("53");
                });
                return true;
            })
            .assertNoErrors();
    }
}
