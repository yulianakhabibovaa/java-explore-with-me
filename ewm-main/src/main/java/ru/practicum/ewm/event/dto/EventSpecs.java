package ru.practicum.ewm.event.dto;

import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;

import java.time.LocalDateTime;
import java.util.List;

@UtilityClass
public class EventSpecs {
    public Specification<Event> hasInitiatorIds(List<Long> initiatorIds) {
        return (root, query, cb) -> root.get("initiator").get("id").in(initiatorIds);
    }

    public Specification<Event> hasState(EventState state) {
        return (root, query, cb) -> cb.equal(root.get("state"), state);
    }

    public Specification<Event> eventDateAfter(LocalDateTime dateTime) {
        return (root, query, cb) -> cb.greaterThan(root.get("eventDate"), dateTime);
    }
}
