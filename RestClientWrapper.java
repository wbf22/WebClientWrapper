package com.access.accessone.common.http;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Component
public class RestClientWrapper {

    private final WebClient webClient;


    public RestClientWrapper() {
        webClient = WebClient.create();
    }

    public RestClientWrapper(PropertyNamingStrategy mappingCase, long timeOutMs) {
        ObjectMapper om = new ObjectMapper();
        om.registerModule( new Jdk8Module() );
        om.setPropertyNamingStrategy( mappingCase );
        // Ignore unknown properties
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false );
        // Add LocalDate ser/des to the object mapper, as detailed here
        // http://lewandowski.io/2016/02/formatting-java-time-with-spring-boot-using-json/
        JavaTimeModule jtm = new JavaTimeModule();
        jtm.addSerializer(LocalDate.class, LocalDateSerializer.INSTANCE );
        jtm.addDeserializer(LocalDate.class, LocalDateDeserializer.INSTANCE );
        om.registerModule( jtm );

        webClient = buildWebClient(om, timeOutMs);
    }

    public RestClientWrapper(ObjectMapper objectMapper, long timeOutMs) {
        webClient = buildWebClient(objectMapper, timeOutMs);
    }

    private static WebClient buildWebClient(ObjectMapper objectMapper, long timeOutMs) {
        HttpClient client = HttpClient.create()
            .responseTimeout(Duration.ofMillis(timeOutMs));

        return WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(client))
            .exchangeStrategies(ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper)))
                .codecs(configurer -> configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper)))
                .build())
            .build();
    }


    public <R> R get(String url, Map<String, String> headers, Class<R> responseType) {
        WebClient.RequestHeadersSpec<?> spec = webClient.get().uri(url);

        if (headers != null)
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                spec = spec.header(entry.getKey(), entry.getValue());
            }

        return spec
            .retrieve()
            .bodyToMono(responseType)
            .block();
    }

    public <B, R> R post(String url, B body, Map<String, String> headers, Class<R> responseType) {
        WebClient.RequestBodySpec spec = webClient.post().uri(url);

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            spec = spec.header(entry.getKey(), entry.getValue());
        }

        return spec
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .retrieve()
            .bodyToMono(responseType)
            .block();
    }

    public <B, R> R put(String url, B body, Map<String, String> headers, Class<R> responseType) {
        WebClient.RequestBodySpec spec = webClient.put().uri(url);

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            spec = spec.header(entry.getKey(), entry.getValue());
        }

        return spec
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .retrieve()
            .bodyToMono(responseType)
            .block();
    }

    public <B, R> R patch(String url, B body, Map<String, String> headers, Class<R> responseType) {
        WebClient.RequestBodySpec spec = webClient.patch().uri(url);

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            spec = spec.header(entry.getKey(), entry.getValue());
        }

        return spec
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .retrieve()
            .bodyToMono(responseType)
            .block();
    }

    public void delete(String url, Map<String, String> headers) {
        WebClient.RequestHeadersSpec<?> spec = webClient.delete().uri(url);

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            spec = spec.header(entry.getKey(), entry.getValue());
        }

        spec.retrieve()
            .bodyToMono(String.class)
            .block();
    }


    /**
     * Method to do get calls for lists. Some reason Java can't do a 'Class<?>' object for lists or maps.
     * So you can't do 'List<String>.class'. In order to get back a list of objects we need to use java's
     * ParameterizedTypeReference class. (provide something like 'new ParameterizedTypeReference<List<Object>>(){}')
     */
    public <R> List<R> getList(String url, Map<String, String> headers, ParameterizedTypeReference<List<R>> responseType) {
        WebClient.RequestHeadersSpec<?> spec = webClient.get().uri(url);

        if (headers != null)
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                spec = spec.header(entry.getKey(), entry.getValue());
            }

        return spec
            .retrieve()
            .bodyToMono(responseType)
            .block();
    }

    /**
     * Method to do post calls for lists. Some reason Java can't get a 'Class<?>' object for lists or maps.
     * So you can't do 'List<String>.class'. In order to get back a list of objects we need to use java's
     * ParameterizedTypeReference class. (provide something like 'new ParameterizedTypeReference<List<Object>>(){}')
     */
    public <B, R> List<R> postForList(String url, B body, Map<String, String> headers, ParameterizedTypeReference<List<R>> responseType) {
        WebClient.RequestBodySpec spec = webClient.post().uri(url);

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            spec = spec.header(entry.getKey(), entry.getValue());
        }

        return spec
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .retrieve()
            .bodyToMono(responseType)
            .block();
    }

    /**
     * Method to do put calls for lists. Some reason Java can't do a 'Class<?>' object for lists or maps.
     * So you can't do 'List<String>.class'. In order to get back a list of objects we need to use java's
     * ParameterizedTypeReference class. (provide something like 'new ParameterizedTypeReference<List<Object>>(){}')
     */
    public <B, R> List<R> putForList(String url, B body, Map<String, String> headers, ParameterizedTypeReference<List<R>> responseType) {
        WebClient.RequestBodySpec spec = webClient.put().uri(url);

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            spec = spec.header(entry.getKey(), entry.getValue());
        }

        return spec
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .retrieve()
            .bodyToMono(responseType)
            .block();
    }


    /**
     * Method to do patch calls for lists. Some reason Java can't do a 'Class<?>' object for lists or maps.
     * So you can't do 'List<String>.class'. In order to get back a list of objects we need to use java's
     * ParameterizedTypeReference class. (provide something like 'new ParameterizedTypeReference<List<Object>>(){}')
     */
    public <B, R> List<R> patchForList(String url, B body, Map<String, String> headers, ParameterizedTypeReference<List<R>> responseType) {
        WebClient.RequestBodySpec spec = webClient.patch().uri(url);

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            spec = spec.header(entry.getKey(), entry.getValue());
        }

        return spec
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .retrieve()
            .bodyToMono(responseType)
            .block();
    }

}

