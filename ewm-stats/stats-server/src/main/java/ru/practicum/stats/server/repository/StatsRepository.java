package ru.practicum.stats.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.stats.server.model.EndpointHit;
import org.springframework.data.repository.query.Param;
import ru.practicum.stats.server.model.ViewStats;

import java.time.LocalDateTime;
import java.util.Collection;

@Repository
public interface StatsRepository extends JpaRepository<EndpointHit, Integer> {
    @Query("SELECT new ru.practicum.stats.server.model.ViewStats(h.app, h.uri, " +
            "CASE WHEN :unique = true THEN COUNT(DISTINCT h.ip) ELSE COUNT(h) END) " +
            "FROM EndpointHit h " +
            "WHERE h.created BETWEEN :start AND :end " +
            "AND (h.uri IN :uris) " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY CASE WHEN :unique = true THEN COUNT(DISTINCT h.ip) ELSE COUNT(h) END DESC")
    Collection<ViewStats> findStatsForUris(@Param("start") LocalDateTime start,
                                           @Param("end") LocalDateTime end,
                                           @Param("uris") Collection<String> uris,
                                           @Param("unique") Boolean unique);

    @Query("SELECT new ru.practicum.stats.server.model.ViewStats(h.app, h.uri, " +
            "CASE WHEN :unique = true THEN COUNT(DISTINCT h.ip) ELSE COUNT(h) END) " +
            "FROM EndpointHit h " +
            "WHERE h.created BETWEEN :start AND :end " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY CASE WHEN :unique = true THEN COUNT(DISTINCT h.ip) ELSE COUNT(h) END DESC")
    Collection<ViewStats> findAllStats(@Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end,
                                       @Param("unique") Boolean unique);

}