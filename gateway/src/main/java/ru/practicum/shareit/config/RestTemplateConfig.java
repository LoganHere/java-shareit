package ru.practicum.shareit.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;

import java.net.http.HttpClient;

@Configuration
public class RestTemplateConfig {
    @Bean
    public RestTemplateBuilder restTemplateBuilder() {
        HttpClient httpClient = HttpClient.newBuilder().build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        return new RestTemplateBuilder().requestFactory(() -> factory);
    }
}