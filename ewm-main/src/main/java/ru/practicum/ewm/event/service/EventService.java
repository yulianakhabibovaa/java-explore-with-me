package ru.practicum.ewm.event.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.event.dto.UpdateEventUserRequest;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    List<EventShortDto> getUserEvents(long userId, int from, int size);

    EventFullDto createEvent(Long userId, NewEventDto newEventDto);

    @Transactional(readOnly = true)
    EventFullDto getUserEvent(long userId, long eventId);

    @Transactional
    EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest updateRequest);

    @Transactional
    EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateRequest);

    @Transactional(readOnly = true)
    EventFullDto getEventById(Long eventId, HttpServletRequest request);

    @Transactional(readOnly = true)
    List<EventFullDto> getFullEvents(
            List<Long> users,
            List<String> states,
            List<Long> categories,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            int from,
            int size
    );

    @Transactional(readOnly = true)
    List<EventShortDto> getShortEvents(String text, List<Long> categories, Boolean paid,
                                       LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                       Boolean onlyAvailable, String sort,
                                       int from, int size, HttpServletRequest request);
}
