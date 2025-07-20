package ru.practicum.ewm.event.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.UpdateEventUserRequest;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.service.ParticipationRequestService;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
@Validated
public class PrivateEventController {
    private final EventService eventService;
    private final ParticipationRequestService requestService;

    @GetMapping
    public List<EventShortDto> getEvents(
            @PathVariable @NotNull @PositiveOrZero Long userId,
            @RequestParam(defaultValue = "0") @Min(0) Integer from,
            @RequestParam(defaultValue = "10") @Min(1) Integer size
    ) {
        return eventService.getUserEvents(userId, from, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createEvent(
            @PathVariable @NotNull @PositiveOrZero Long userId,
            @Valid @RequestBody NewEventDto newEventDto
    ) {
        return eventService.createEvent(userId, newEventDto);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEvent(
            @PathVariable @NotNull @PositiveOrZero Long userId,
            @PathVariable @NotNull @PositiveOrZero Long eventId,
            @Valid @RequestBody UpdateEventUserRequest updateRequest
    ) {
        return eventService.updateEventByUser(userId, eventId, updateRequest);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEvent(
            @PathVariable @NotNull @PositiveOrZero Long userId,
            @PathVariable @NotNull @PositiveOrZero Long eventId
    ) {
        return eventService.getUserEvent(userId, eventId);
    }

    @GetMapping("/{eventId}/requests")
    public Collection<ParticipationRequestDto> getEventRequests(
            @PathVariable @NotNull @PositiveOrZero Long userId,
            @PathVariable @NotNull @PositiveOrZero Long eventId
    ) {
        return requestService.getEventRequests(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult updateRequestStatus(
            @PathVariable @NotNull @PositiveOrZero Long userId,
            @PathVariable @NotNull @PositiveOrZero Long eventId,
            @RequestBody @Valid EventRequestStatusUpdateRequest updateRequest) {
        return requestService.updateRequestStatus(userId, eventId, updateRequest);
    }
}
