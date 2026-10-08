package com.spamer.outer;

import com.spamer.domain.ProxyEntity;
import com.spamer.domain.ServiceEntity;
import com.spamer.domain.ServiceIntegrationKind;
import com.spamer.outer.integration.NodeVerifiedOtpClient;
import com.spamer.outer.integration.OzonFastEntryClient;
import com.spamer.outer.integration.PhoneNormalizer;
import com.spamer.outer.integration.YandexPassportOtpClient;
import io.netty.channel.ChannelOption;
import java.time.Duration;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.ProxyProvider;

@Component
public class OuterApiClient {

    private static final Duration TIMEOUT = Duration.ofSeconds(15);

    private final YandexPassportOtpClient yandexPassportOtpClient;
    private final OzonFastEntryClient ozonFastEntryClient;
    private final NodeVerifiedOtpClient nodeVerifiedOtpClient;

    public OuterApiClient(
            YandexPassportOtpClient yandexPassportOtpClient,
            OzonFastEntryClient ozonFastEntryClient,
            NodeVerifiedOtpClient nodeVerifiedOtpClient) {
        this.yandexPassportOtpClient = yandexPassportOtpClient;
        this.ozonFastEntryClient = ozonFastEntryClient;
        this.nodeVerifiedOtpClient = nodeVerifiedOtpClient;
    }

    public OuterApiResult send(ServiceEntity service, String target, ProxyEntity proxy) {
        ServiceIntegrationKind integration = service.getIntegration();
        if (integration == ServiceIntegrationKind.YANDEX_PWL_OTP) {
            return yandexPassportOtpClient.sendOtp(target, proxy);
        }
        if (integration == ServiceIntegrationKind.OZON_FAST_ENTRY) {
            return ozonFastEntryClient.sendOtp(target, proxy);
        }
        if (isNodeVerified(integration)) {
            return nodeVerifiedOtpClient.send(integration, target, proxy);
        }
        return sendGeneric(service, target, proxy);
    }

    private static boolean isNodeVerified(ServiceIntegrationKind integration) {
        return integration == ServiceIntegrationKind.KORONAPAY_OTP
                || integration == ServiceIntegrationKind.START_RU_OTP
                || integration == ServiceIntegrationKind.QLEAN_OTP
                || integration == ServiceIntegrationKind.MTS_TV_OTP
                || integration == ServiceIntegrationKind.WEBBANKIR_OTP;
    }

    private OuterApiResult sendGeneric(ServiceEntity service, String target, ProxyEntity proxy) {
        long start = System.currentTimeMillis();
        try {
            HttpClient httpClient = HttpClient.create()
                    .responseTimeout(TIMEOUT)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);

            if (proxy != null) {
                httpClient = httpClient.proxy(spec -> spec
                        .type(ProxyProvider.Proxy.HTTP)
                        .host(proxy.getHost())
                        .port(proxy.getPort())
                        .username(proxy.getUsername())
                        .password(user -> proxy.getPassword()));
            }

            WebClient client = WebClient.builder()
                    .clientConnector(new ReactorClientHttpConnector(httpClient))
                    .baseUrl(service.getBaseUrl())
                    .defaultHeader("User-Agent", com.spamer.outer.integration.BrowserHeaders.USER_AGENT)
                    .build();

            String body = buildBody(service, target);

            var response = client.method(HttpMethod.valueOf(service.getHttpMethod()))
                    .uri(service.getEndpointPath())
                    .header("Content-Type", "application/json")
                    .bodyValue(body)
                    .exchangeToMono(clientResponse -> clientResponse.bodyToMono(String.class)
                            .defaultIfEmpty("")
                            .map(responseBody -> new OuterApiResult(
                                    clientResponse.statusCode().value(),
                                    System.currentTimeMillis() - start,
                                    null)))
                    .block(TIMEOUT);

            return response != null ? response : new OuterApiResult(0, 0L, "Empty response");
        } catch (Exception ex) {
            return new OuterApiResult(0, System.currentTimeMillis() - start, ex.getMessage());
        }
    }

    private String buildBody(ServiceEntity service, String target) {
        if (service.getRequestBodyTemplate() != null) {
            return service.getRequestBodyTemplate()
                    .replace("{{phone}}", target)
                    .replace("{{phone_e164}}", PhoneNormalizer.toE164(target))
                    .replace("{{email}}", target);
        }
        return "{\"phone\":\"" + target + "\"}";
    }
}
