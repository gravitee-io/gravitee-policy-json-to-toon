package io.gravitee.policy.jsontotoon;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.toonformat.jtoon.DecodeOptions;
import dev.toonformat.jtoon.Delimiter;
import dev.toonformat.jtoon.EncodeOptions;
import dev.toonformat.jtoon.JToon;
import dev.toonformat.jtoon.KeyFolding;
import dev.toonformat.jtoon.PathExpansion;
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

    public Completable onMessageRequest(HttpMessageExecutionContext ctx) {
        return ctx.request().onMessage(message -> transformMessage(message, ctx));
    }

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
                Delimiter.valueOf(configuration.getDelimiter()),
                configuration.isLengthMarker(),
                KeyFolding.valueOf(configuration.getFlatten()),
                configuration.getFlattenDepth()
            );
            return JToon.encodeJson(content, options);
        } else if (conversion == JsonToToonPolicyConfiguration.Conversion.TOON_TO_JSON) {
            DecodeOptions options = new DecodeOptions(
                configuration.getIndent(),
                Delimiter.valueOf(configuration.getDelimiter()),
                configuration.isStrict(),
                PathExpansion.valueOf(configuration.getExpandPaths())
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
