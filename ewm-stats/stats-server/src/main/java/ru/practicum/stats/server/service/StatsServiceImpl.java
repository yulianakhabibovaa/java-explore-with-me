package ru.practicum.stats.server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.stats.server.exception.BadRequestException;
import ru.practicum.stats.server.mapper.StatsMapper;
import ru.practicum.stats.server.model.EndpointHit;
import ru.practicum.stats.server.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {
    private final StatsRepository repository;

    @Override
    public void saveHit(EndpointHitDto event) {
        EndpointHit endpointHit = StatsMapper.toEndpointHit(event);
        EndpointHit result = repository.save(endpointHit);
        log.info("Сохранен вызов {}", result);
    }

    @Override
    public Collection<ViewStatsDto> getStatistics(LocalDateTime start, LocalDateTime end, Collection<String> uris, Boolean unique) {
        if (start == null || end == null || start.isAfter(end)) {
            throw new BadRequestException("Некорректные входные данные");
        }
        if (uris == null || uris.isEmpty()) {
            return StatsMapper.toViewStatsDtos(repository.findAllStats(start, end, unique));
        }
        return StatsMapper.toViewStatsDtos(repository.findStatsForUris(start, end, uris, unique));
    }
}
