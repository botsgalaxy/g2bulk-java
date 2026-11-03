package com.g2bulk;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class JdkTransport implements Transport {
    private final HttpClient client;

    JdkTransport(Duration connectTimeout) {
        this.client = HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    @Override
    public TransportResponse send(TransportRequest request) throws IOException {
        HttpRequest.BodyPublisher publisher = request.body() == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofByteArray(request.body());
        HttpRequest.Builder builder = HttpRequest.newBuilder(request.uri())
                .timeout(request.timeout())
                .method(request.method(), publisher);
        if (request.uri().getScheme().equalsIgnoreCase("http")) builder.version(HttpClient.Version.HTTP_1_1);
        for (Map.Entry<String, String> e : request.headers().entrySet()) builder.header(e.getKey(), e.getValue());
        HttpResponse<byte[]> response;
        try {
            response = client.send(builder.build(), HttpResponse.BodyHandlers.ofByteArray());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Request interrupted", e);
        }
        Map<String, String> headers = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> e : response.headers().map().entrySet()) {
            if (!e.getValue().isEmpty()) headers.put(e.getKey(), e.getValue().get(0));
        }
        return new TransportResponse(response.statusCode(), headers, response.body());
    }
}
