package ru.practicum.stats.server.service;

import org.springframework.stereotype.Service;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.Collection;

@Service
public interface StatsService {
    void saveHit(EndpointHitDto event);

    Collection<ViewStatsDto> getStatistics(LocalDateTime start, LocalDateTime end, Collection<String> uris, Boolean unique);
}
