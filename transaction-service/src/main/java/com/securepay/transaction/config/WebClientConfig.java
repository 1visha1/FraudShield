package com.securepay.transaction.config;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;

@Configuration
public class WebClientConfig {

    @Value("${server.ssl.enabled:false}")
    private boolean sslEnabled;

    @Value("${server.ssl.key-store:#{null}}")
    private Resource keyStore;
    @Value("${server.ssl.key-store-password:}")
    private String keyStorePassword;
    @Value("${server.ssl.trust-store:#{null}}")
    private Resource trustStore;
    @Value("${server.ssl.trust-store-password:}")
    private String trustStorePassword;

    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() throws Exception {
        if (!sslEnabled) {
            return WebClient.builder();
        }

        SslContext sslContext = SslContextBuilder.forClient()
                .keyManager(keyManagerFactory())
                .trustManager(trustManagerFactory())
                .build();

        HttpClient httpClient = HttpClient.create().secure(t -> t.sslContext(sslContext));
        return WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient));
    }

    private KeyManagerFactory keyManagerFactory() throws Exception {
        KeyStore ks = KeyStore.getInstance("PKCS12");
        try (InputStream is = keyStore.getInputStream()) {
            ks.load(is, keyStorePassword.toCharArray());
        }
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, keyStorePassword.toCharArray());
        return kmf;
    }

    private TrustManagerFactory trustManagerFactory() throws Exception {
        KeyStore ts = KeyStore.getInstance("PKCS12");
        try (InputStream is = trustStore.getInputStream()) {
            ts.load(is, trustStorePassword.toCharArray());
        }
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ts);
        return tmf;
    }
}