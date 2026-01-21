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

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.toonformat.jtoon.DecodeOptions;
import dev.toonformat.jtoon.EncodeOptions;
import dev.toonformat.jtoon.JToon;
import io.gravitee.common.http.HttpStatusCode;
import io.gravitee.gateway.api.buffer.Buffer;
import io.gravitee.gateway.api.http.HttpHeaderNames;
import io.gravitee.gateway.api.http.HttpHeaders;
import io.gravitee.gateway.reactive.api.ExecutionFailure;
import io.gravitee.gateway.reactive.api.context.http.HttpMessageExecutionContext;
import io.gravitee.gateway.reactive.api.context.http.HttpPlainExecutionContext;
import io.gravitee.gateway.reactive.api.message.Message;
import io.gravitee.gateway.reactive.api.policy.http.HttpPolicy;
import io.gravitee.policy.jsontotoon.configuration.JsonToToonPolicyConfiguration;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;

public class JsonToToonPolicy implements HttpPolicy {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String TEXT_TOON = "text/toon";
    private static final String APPLICATION_JSON = "application/json";
    private final JsonToToonPolicyConfiguration configuration;

    public JsonToToonPolicy(JsonToToonPolicyConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public String id() {
        return "json-to-toon";
    }

    @Override
    public Completable onRequest(HttpPlainExecutionContext ctx) {
        return ctx.request().onBody(body -> applyConversion(body, ctx, ctx.request().headers()));
    }

    @Override
    public Completable onResponse(HttpPlainExecutionContext ctx) {
        return ctx.response().onBody(body -> applyConversion(body, ctx, ctx.response().headers()));
    }

    private Maybe<Buffer> applyConversion(Maybe<Buffer> body, HttpPlainExecutionContext ctx, HttpHeaders headers) {
        return body
            .flatMap(buffer -> {
                if (buffer != null && buffer.length() > 0) {
                    return Maybe.fromCallable(() -> Buffer.buffer(convert(buffer.toString())));
                }
                return Maybe.empty();
            })
            .doOnSuccess(buffer -> setContentHeaders(headers, buffer.length()))
            .onErrorResumeNext(ioe ->
                ctx.interruptBodyWith(
                    new ExecutionFailure(HttpStatusCode.INTERNAL_SERVER_ERROR_500)
                        .key("JSON_TO_TOON_ERROR")
                        .message("Unable to perform conversion")
                        .cause(ioe)
                )
            );
    }

    @Override
    public Completable onMessageRequest(HttpMessageExecutionContext ctx) {
        return ctx.request().onMessage(message -> transformMessage(message, ctx));
    }

    @Override
    public Completable onMessageResponse(HttpMessageExecutionContext ctx) {
        return ctx.response().onMessage(message -> transformMessage(message, ctx));
    }

    private Maybe<Message> transformMessage(Message message, HttpMessageExecutionContext ctx) {
        if (message.content() == null) {
            return Maybe.just(message);
        }

        return Maybe.fromCallable(() -> {
            String content = message.content().toString();
            String transformedContent = convert(content);
            Buffer buffer = Buffer.buffer(transformedContent);
            setContentHeaders(message.headers(), buffer.length());
            return message.content(buffer);
        }).onErrorResumeNext(ioe ->
            ctx.interruptMessageWith(
                new ExecutionFailure(HttpStatusCode.INTERNAL_SERVER_ERROR_500)
                    .key("JSON_TO_TOON_ERROR")
                    .message("Unable to perform conversion")
                    .cause(ioe)
            )
        );
    }

    private void setContentHeaders(HttpHeaders headers, int contentLength) {
        if (configuration.getConversion() == JsonToToonPolicyConfiguration.Conversion.JSON_TO_TOON) {
            headers.set(HttpHeaderNames.CONTENT_TYPE, TEXT_TOON);
        } else if (configuration.getConversion() == JsonToToonPolicyConfiguration.Conversion.TOON_TO_JSON) {
            headers.set(HttpHeaderNames.CONTENT_TYPE, APPLICATION_JSON);
        }
        headers.set(HttpHeaderNames.CONTENT_LENGTH, Integer.toString(contentLength));
    }

    private String convert(String content) throws Exception {
        JsonToToonPolicyConfiguration.Conversion conversion = configuration.getConversion();

        if (conversion == JsonToToonPolicyConfiguration.Conversion.JSON_TO_TOON) {
            EncodeOptions options = new EncodeOptions(
                configuration.getIndent(),
                configuration.getDelimiter(),
                configuration.isLengthMarker(),
                configuration.getFlatten(),
                configuration.getFlattenDepth()
            );
            return JToon.encodeJson(content, options);
        } else if (conversion == JsonToToonPolicyConfiguration.Conversion.TOON_TO_JSON) {
            DecodeOptions options = new DecodeOptions(
                configuration.getIndent(),
                configuration.getDelimiter(),
                configuration.isStrict(),
                configuration.getExpandPaths()
            );
            Object decoded = JToon.decode(content, options);
            if (configuration.isPrettyPrint()) {
                return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(decoded);
            }
            return MAPPER.writeValueAsString(decoded);
        }
        return content;
    }
}
