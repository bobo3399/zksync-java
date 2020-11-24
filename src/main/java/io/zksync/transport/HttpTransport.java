package io.zksync.transport;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.zksync.exception.ZkSyncException;
import okhttp3.*;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

public class HttpTransport implements ZkSyncTransport {

    private final OkHttpClient httpClient;

    private final String url;

    private final ObjectMapper objectMapper;

    public HttpTransport(String url) {
        this.url = url;

        httpClient = new OkHttpClient
                .Builder()
                .callTimeout(Duration.ofSeconds(5))
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        objectMapper = new ObjectMapper();
    }

    @Override
    public <T> T send(String method, List<Object> params, Class<T> returntype) {
        try {
            final ZkSyncRequest zkRequest = ZkSyncRequest
                    .builder()
                    .method(method)
                    .params(params)
                    .build();

            final RequestBody body = RequestBody.create(objectMapper.writeValueAsString(zkRequest),
                    MediaType.parse("application/json"));

            final Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            //TODO gotta sort out this double parsing
            //new TypeReference<ZkSyncResponse<T>>() {} doesn't work
            final Response response = httpClient.newCall(request).execute();

            final String responseString = response.body().string();

            final ZkSyncResponse<JsonNode> resultJson = objectMapper.readValue(responseString,
                    new TypeReference<ZkSyncResponse<JsonNode>>() {});

            if (resultJson.getError() != null) {
                throw new ZkSyncException(resultJson.getError());
            }

            return objectMapper.readValue(resultJson.getResult().toString(), returntype);

        } catch (IOException e) {
            throw new ZkSyncException("There was an error when sending the request", e);
        }
    }
}