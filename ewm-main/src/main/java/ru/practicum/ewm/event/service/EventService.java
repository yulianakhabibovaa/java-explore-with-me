package ru.practicum.ewm.event.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.ewm.event.dto.AdminEventsRequestDto;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.EventsRequestDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.event.dto.UpdateEventUserRequest;

import java.util.List;

public interface EventService {
    List<EventShortDto> getUserEvents(long userId, int from, int size);

    EventFullDto createEvent(Long userId, NewEventDto newEventDto);

    EventFullDto getUserEvent(long userId, long eventId);

    EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest updateRequest);

    EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateRequest);

    EventFullDto getEventById(Long eventId, HttpServletRequest request);

    List<EventFullDto> getFullEvents(AdminEventsRequestDto params);

    List<EventShortDto> getShortEvents(EventsRequestDto params);

    List<EventShortDto> getPublishedEventsByAuthors(List<Long> authorIds, int from, int size);
}
