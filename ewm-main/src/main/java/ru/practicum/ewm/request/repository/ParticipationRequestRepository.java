package ru.practicum.ewm.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.request.model.ParticipationRequest;
import ru.practicum.ewm.request.model.RequestStatus;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {
    boolean existsByEventIdAndRequesterId(Long eventId, Long requesterId);

    Collection<ParticipationRequest> findByRequesterId(Long requesterId);

    Collection<ParticipationRequest> findByEventIdAndEventInitiatorId(Long eventId, Long initiatorId);

    Collection<ParticipationRequest> findByIdIn(Collection<Long> ids);

    Optional<ParticipationRequest> findByEventIdAndRequesterId(Long eventId, Long requesterId);

    Long countByEventIdAndStatus(Long eventId, RequestStatus status);
}
