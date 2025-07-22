package ru.practicum.ewm.event.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.event.dto.AdminEventsRequestDto;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.event.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
@Validated
public class AdminEventController {
    private final EventService eventService;

    @GetMapping
    public List<EventFullDto> getEvents(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<String> states,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size
    ) {
        LocalDateTime start = (rangeStart != null) ? rangeStart : LocalDateTime.now();
        LocalDateTime end = (rangeEnd != null) ? rangeEnd : LocalDateTime.now().plusYears(10);
        AdminEventsRequestDto params = new AdminEventsRequestDto();
        params.setUsers(users);
        params.setStates(states);
        params.setCategories(categories);
        params.setRangeStart(start);
        params.setRangeEnd(end);
        params.setFrom(from);
        params.setSize(size);
        return eventService.getFullEvents(params);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEvent(
            @PathVariable @PositiveOrZero Long eventId,
            @Valid @RequestBody UpdateEventAdminRequest updateRequest
    ) {
        return eventService.updateEventByAdmin(eventId, updateRequest);
    }
}