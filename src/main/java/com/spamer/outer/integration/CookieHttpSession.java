package com.spamer.outer.integration;

import com.spamer.domain.ProxyEntity;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public class CookieHttpSession {

    private static final Duration TIMEOUT = Duration.ofSeconds(15);

    private final HttpClient client;

    public CookieHttpSession(ProxyEntity proxy) {
        HttpClient.Builder builder = HttpClient.newBuilder()
                .connectTimeout(TIMEOUT)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .cookieHandler(new java.net.CookieManager(null, java.net.CookiePolicy.ACCEPT_ALL));

        if (proxy != null) {
            builder.proxy(new FixedProxySelector(proxy.getHost(), proxy.getPort()));
            if (proxy.getUsername() != null && !proxy.getUsername().isBlank()) {
                builder.authenticator(new ProxyAuthenticator(proxy.getUsername(), proxy.getPassword()));
            }
        }

        this.client = builder.build();
    }

    public HttpExchange get(String url, Map<String, String> headers) throws IOException, InterruptedException {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(url)).timeout(TIMEOUT).GET();
        headers.forEach(request::header);
        long start = System.currentTimeMillis();
        HttpResponse<String> response = client.send(request.build(), HttpResponse.BodyHandlers.ofString());
        return new HttpExchange(response.statusCode(), System.currentTimeMillis() - start, response.body());
    }

    public HttpExchange postJson(String url, String json, Map<String, String> headers)
            throws IOException, InterruptedException {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(url))
                .timeout(TIMEOUT)
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8));
        headers.forEach(request::header);
        long start = System.currentTimeMillis();
        HttpResponse<String> response = client.send(request.build(), HttpResponse.BodyHandlers.ofString());
        return new HttpExchange(response.statusCode(), System.currentTimeMillis() - start, response.body());
    }

    public HttpExchange postForm(String url, String formBody, Map<String, String> headers)
            throws IOException, InterruptedException {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(url))
                .timeout(TIMEOUT)
                .POST(HttpRequest.BodyPublishers.ofString(formBody, StandardCharsets.UTF_8));
        headers.forEach(request::header);
        long start = System.currentTimeMillis();
        HttpResponse<String> response = client.send(request.build(), HttpResponse.BodyHandlers.ofString());
        return new HttpExchange(response.statusCode(), System.currentTimeMillis() - start, response.body());
    }

    public HttpExchange postEmpty(String url, Map<String, String> headers)
            throws IOException, InterruptedException {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(url))
                .timeout(TIMEOUT)
                .POST(HttpRequest.BodyPublishers.noBody());
        headers.forEach(request::header);
        long start = System.currentTimeMillis();
        HttpResponse<String> response = client.send(request.build(), HttpResponse.BodyHandlers.ofString());
        return new HttpExchange(response.statusCode(), System.currentTimeMillis() - start, response.body());
    }

    public record HttpExchange(int statusCode, long responseTimeMs, String body) {
    }

    private static final class ProxyAuthenticator extends java.net.Authenticator {

        private final String username;
        private final String password;

        private ProxyAuthenticator(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        protected java.net.PasswordAuthentication getPasswordAuthentication() {
            return new java.net.PasswordAuthentication(username, password.toCharArray());
        }
    }

    private static final class FixedProxySelector extends ProxySelector {

        private final Proxy proxy;

        private FixedProxySelector(String host, int port) {
            this.proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
        }

        @Override
        public List<Proxy> select(URI uri) {
            return List.of(proxy);
        }

        @Override
        public void connectFailed(URI uri, java.net.SocketAddress sa, IOException ioe) {
            // no-op
        }
    }
}
