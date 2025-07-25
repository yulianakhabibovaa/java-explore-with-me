package ru.practicum.stats.server.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.stats.server.service.StatsService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;

@Slf4j
@RestController
@RequiredArgsConstructor
public class StatsController {
    private final StatsService statsService;
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/hit")
    public void saveHit(@RequestBody EndpointHitDto hit) {
        log.info("Сохранение события вызова сервиса {}", hit);
        statsService.saveHit(hit);
    }

    @GetMapping("/stats")
    public Collection<ViewStatsDto> getStatistics(@RequestParam String start,
                                                  @RequestParam String end,
                                                  @RequestParam(required = false) Collection<String> uris,
                                                  @RequestParam(defaultValue = "false") Boolean unique) {
        log.info("Запрос статистики: start={}, end={}, uris={}, unique={}", start, end, uris, unique);
        return statsService.getStatistics(
                LocalDateTime.parse(start, dateTimeFormatter), LocalDateTime.parse(end, dateTimeFormatter), uris, unique
        );
    }
}
