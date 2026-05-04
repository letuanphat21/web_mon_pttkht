package org.example.webquanao.helper;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class HttpClientHelper {
    // POST request
    public static String post(String url, String body) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(url);
            post.setHeader("Content-Type", "application/x-www-form-urlencoded");
            post.setEntity(new StringEntity(body, StandardCharsets.UTF_8));

            HttpClientResponseHandler<String> handler = (ClassicHttpResponse response) ->
                    new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
            return client.execute(post, handler);
        }
    }

    // GET request
    public static String get(String url) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet get = new HttpGet(url);
            HttpClientResponseHandler<String> handler = (ClassicHttpResponse response) ->
                    new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);

            return client.execute(get, handler);
        }
    }
}
