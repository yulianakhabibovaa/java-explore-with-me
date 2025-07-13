package ru.practicum.stats.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.stats.dto.EndpointHitDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
public class StatsClient {
    private final RestTemplate rest;
    private static final String BASE_PATH = "/";
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    public StatsClient(@Value("${stats-server.url}") String serverUrl, RestTemplateBuilder builder, RestTemplate rest) {
        this.rest = builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + BASE_PATH))
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                .build();
    }

    public ResponseEntity<Object> getStats(LocalDateTime start, LocalDateTime end, Collection<String> uris, Boolean unique) {
        Map<String, Object> parameters = Map.of(
                "start", start.format(dateTimeFormatter),
                "end", end.format(dateTimeFormatter),
                "uris", String.join(",", uris),
                "unique", unique != null && unique
        );
        return makeAndSendRequest(HttpMethod.GET, "/stats", parameters, null);
    }

    public ResponseEntity<Object> postHit(EndpointHitDto hitDto) {
        return makeAndSendRequest(HttpMethod.POST, "/hit", null, hitDto);
    }

    private <T> ResponseEntity<Object> makeAndSendRequest(HttpMethod method, String path, @Nullable Map<String, Object> parameters, @Nullable T body) {
        HttpEntity<T> requestEntity = new HttpEntity<>(body, defaultHeaders());

        ResponseEntity<Object> statsServerResponse;
        try {
            if (parameters != null) {
                statsServerResponse = rest.exchange(path, method, requestEntity, Object.class, parameters);
            } else {
                statsServerResponse = rest.exchange(path, method, requestEntity, Object.class);
            }
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsByteArray());
        }
        return prepareResponse(statsServerResponse);
    }

    private static ResponseEntity<Object> prepareResponse(ResponseEntity<Object> response) {
        if (response.getStatusCode().is2xxSuccessful()) {
            return response;
        }

        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.status(response.getStatusCode());

        if (response.hasBody()) {
            return responseBuilder.body(response.getBody());
        }

        return responseBuilder.build();
    }

    private HttpHeaders defaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }
}
