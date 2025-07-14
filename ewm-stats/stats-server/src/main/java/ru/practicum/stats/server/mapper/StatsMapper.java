package ru.practicum.stats.server.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.stats.server.model.EndpointHit;
import ru.practicum.stats.server.model.ViewStats;

import java.util.Collection;
import java.util.List;

@UtilityClass
public class StatsMapper {
    public static EndpointHitDto toEndpointHitDto(EndpointHit hit) {
        return EndpointHitDto.builder()
                .app(hit.getApp())
                .id(hit.getId())
                .ip(hit.getIp())
                .timestamp(hit.getCreated())
                .uri(hit.getUri())
                .build();
    }

    public static EndpointHit toEndpointHit(EndpointHitDto hit) {
        return EndpointHit.builder()
                .app(hit.getApp())
                .id(hit.getId())
                .ip(hit.getIp())
                .created(hit.getTimestamp())
                .uri(hit.getUri())
                .build();
    }

    public static List<ViewStatsDto> toViewStatsDtos(Collection<ViewStats> stats) {
        return stats.stream().map(StatsMapper::toViewStatsDto).toList();
    }

    public static ViewStatsDto toViewStatsDto(ViewStats stats) {
        return new ViewStatsDto(stats.getApp(), stats.getUri(), stats.getHits());
    }
}
