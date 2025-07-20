package ru.practicum.ewm.event.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    boolean existsByIdAndInitiatorId(Long eventId, Long userId);

    @Query(value = "SELECT * FROM categories ORDER BY id OFFSET :offset LIMIT :limit", nativeQuery = true)
    List<Event> findByInitiatorIdWithOffset(Long userId, @Param("offset") int offset, @Param("limit") int limit);

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long userId);

    @Query("SELECT COUNT(r) FROM ParticipationRequest r " +
            "WHERE r.event.id = :eventId AND r.status = 'CONFIRMED'")
    long countConfirmedRequests(@Param("eventId") Long eventId);

    @Query("SELECT e FROM Event e " +
            "WHERE e.state = 'PUBLISHED' " +
            "AND (:text IS NULL OR (e.annotation ILIKE %:text% " +
            "OR e.description ILIKE %:text%)) " +
            "AND (:categories IS NULL OR e.category.id IN :categories) " +
            "AND (:paid IS NULL OR e.paid = :paid) " +
            "AND (CAST(:rangeStart AS timestamp) IS NULL OR e.eventDate >= :rangeStart) " +
            "AND (CAST(:rangeEnd AS timestamp) IS NULL OR e.eventDate <= :rangeEnd) " +
            "AND (:onlyAvailable = FALSE OR (e.participantLimit = 0 OR e.participantLimit > " +
            "(SELECT COUNT(r) FROM ParticipationRequest r WHERE r.event.id = e.id AND r.status = 'CONFIRMED')))")
    List<Event> findPublicEvents(
            @Param("text") String text,
            @Param("categories") Collection<Long> categories,
            @Param("paid") Boolean paid,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            @Param("onlyAvailable") Boolean onlyAvailable,
            PageRequest pageRequest
    );

    @Query("SELECT e FROM Event e " +
            "WHERE (:users IS NULL OR e.initiator.id IN :users) " +
            "AND (:states IS NULL OR e.state IN :states) " +
            "AND (:categories IS NULL OR e.category.id IN :categories) " +
            "AND (:rangeStart IS NULL OR e.eventDate >= :rangeStart) " +
            "AND (:rangeEnd IS NULL OR e.eventDate <= :rangeEnd)")
    List<Event> findEventsByAdminFilters(
            @Param("users") List<Long> users,
            @Param("states") List<EventState> states,
            @Param("categories") List<Long> categories,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            PageRequest pageRequest
    );
}