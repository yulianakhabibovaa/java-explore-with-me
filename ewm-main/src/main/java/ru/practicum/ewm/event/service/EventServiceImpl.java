package ru.practicum.ewm.event.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.error.ConflictException;
import ru.practicum.ewm.error.ForbiddenException;
import ru.practicum.ewm.error.NotFoundException;
import ru.practicum.ewm.error.ValidationException;
import ru.practicum.ewm.event.dto.AdminEventsRequestDto;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.EventSpecs;
import ru.practicum.ewm.event.dto.EventsRequestDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.event.dto.UpdateEventUserRequest;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.request.model.RequestStatus;
import ru.practicum.ewm.request.repository.ParticipationRequestRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ParticipationRequestRepository requestRepository;
    private final StatsClient statsClient;

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getUserEvents(long userId, int from, int size) {
        userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("User not found"));
        return EventMapper.toShortDtoList(eventRepository.findByInitiatorIdWithOffset(userId, from, size));
    }

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Category not found"));

        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ForbiddenException("Event date must be at least 2 hours from now");
        }

        Event event = EventMapper.toEvent(newEventDto, category, initiator, LocalDateTime.now(), EventState.PENDING);
        return EventMapper.toFullDto(eventRepository.save(event));
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getUserEvent(long userId, long eventId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        event.setConfirmedRequests(requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED));
        event.setViews(getViews(eventId));

        return EventMapper.toFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest updateRequest) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if (event.getState() != EventState.PENDING && event.getState() != EventState.CANCELED) {
            throw new ConflictException("Only pending or canceled events can be changed");
        }

        if (updateRequest.getEventDate() != null
                && updateRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ForbiddenException("Event date must be at least 2 hours from now");
        }

        updateEventFields(event, updateRequest);
        if (updateRequest.getStateAction() != null) {
            if (updateRequest.getStateAction() == UpdateEventUserRequest.StateAction.SEND_TO_REVIEW) {
                event.setState(EventState.PENDING);
            } else if (updateRequest.getStateAction() == UpdateEventUserRequest.StateAction.CANCEL_REVIEW) {
                event.setState(EventState.CANCELED);
            }
        }

        Event updatedEvent = eventRepository.save(event);
        updatedEvent.setConfirmedRequests(requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED));
        updatedEvent.setViews(getViews(eventId));
        return EventMapper.toFullDto(updatedEvent);
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if (updateRequest.getStateAction() != null) {
            if (updateRequest.getStateAction() == UpdateEventAdminRequest.StateAction.PUBLISH_EVENT) {
                if (event.getState() != EventState.PENDING) {
                    throw new ConflictException("Cannot publish event that is not pending");
                }
                if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                    throw new ForbiddenException("Event date must be at least 1 hour after publication");
                }
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            } else if (updateRequest.getStateAction() == UpdateEventAdminRequest.StateAction.REJECT_EVENT) {
                if (event.getState() == EventState.PUBLISHED) {
                    throw new ConflictException("Cannot reject published event");
                }
                event.setState(EventState.CANCELED);
            }
        }

        updateEventFields(event, updateRequest);
        Event updatedEvent = eventRepository.save(event);
        updatedEvent.setConfirmedRequests(requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED));
        updatedEvent.setViews(getViews(eventId));
        return EventMapper.toFullDto(updatedEvent);
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEventById(Long eventId, HttpServletRequest request) {
        Event event = eventRepository.findById(eventId)
                .filter(e -> e.getState() == EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        statsClient.postHit(createEndpointHit(request));

        event.setViews(getViews(eventId));
        event.setConfirmedRequests(requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED));

        return EventMapper.toFullDto(event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> getFullEvents(
            AdminEventsRequestDto params
    ) {

        List<EventState> stateEnums = null;
        if (params.getStates() != null) {
            stateEnums = params.getStates().stream()
                    .map(EventState::valueOf)
                    .toList();
        }

        PageRequest pageRequest = PageRequest.of(0, params.getFrom() + params.getSize());

        List<Event> events = eventRepository.findEventsByAdminFilters(
                params.getUsers(),
                stateEnums,
                params.getCategories(),
                params.getRangeStart(),
                params.getRangeEnd(),
                pageRequest
        );

        List<Long> eventIds = events.stream().map(Event::getId).toList();
        Map<Long, Long> views = getViews(eventIds);
        Map<Long, Long> confirmedRequests = getConfirmedRequests(eventIds);

        return events.stream()
                .skip(params.getFrom())
                .limit(params.getSize())
                .map(event -> {
                    EventFullDto dto = EventMapper.toFullDto(event);
                    dto.setViews(views.getOrDefault(event.getId(), 0L));
                    dto.setConfirmedRequests(confirmedRequests.getOrDefault(event.getId(), 0L));
                    return dto;
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getShortEvents(EventsRequestDto params) {
        if (params.getRangeStart().isAfter(params.getRangeEnd())) {
            throw new ValidationException("Start date must be before end date");
        }

        statsClient.postHit(createEndpointHit(params.getRequest()));

        PageRequest pageRequest = PageRequest.of(
                0, params.getFrom() + params.getSize()
        );
        List<Event> events = eventRepository.findPublicEvents(
                params.getText(), params.getCategories(), params.getPaid(), params.getRangeStart(), params.getRangeEnd(),
                params.getOnlyAvailable(), pageRequest
        ).stream().skip(params.getFrom()).limit(params.getSize()).toList();

        if (events.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> eventIds = events.stream().map(Event::getId).toList();
        Map<Long, Long> views = getViews(eventIds);
        Map<Long, Long> confirmedRequests = getConfirmedRequests(eventIds);

        return events.stream()
                .map(event -> {
                    EventShortDto dto = EventMapper.toShortDto(event);
                    dto.setViews(views.getOrDefault(event.getId(), 0L));
                    dto.setConfirmedRequests(confirmedRequests.getOrDefault(event.getId(), 0L));
                    return dto;
                })
                .toList();
    }

    @Override
    public List<EventShortDto> getPublishedEventsByAuthors(List<Long> authorIds, int from, int size) {
        Specification<Event> spec = Specification.where(
                EventSpecs.hasInitiatorIds(authorIds)
                        .and(EventSpecs.hasState(EventState.PUBLISHED))
                        .and(EventSpecs.eventDateAfter(LocalDateTime.now()))
        );

        Pageable pageable = PageRequest.of(0, from + size);

        Page<Event> events = eventRepository.findAll(spec, pageable);

        return events.stream()
                .skip(from)
                .limit(size)
                .map(EventMapper::toShortDto)
                .toList();
    }

    private void updateEventFields(Event event, UpdateEventAdminRequest request) {
        if (request.getAnnotation() != null) event.setAnnotation(request.getAnnotation());
        if (request.getCategory() != null) {
            Category category = categoryRepository.findById(request.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category not found"));
            event.setCategory(category);
        }
        if (request.getDescription() != null) event.setDescription(request.getDescription());
        if (request.getEventDate() != null) event.setEventDate(request.getEventDate());
        if (request.getLocation() != null) event.setLocation(request.getLocation());
        if (request.getPaid() != null) event.setPaid(request.getPaid());
        if (request.getParticipantLimit() != null) event.setParticipantLimit(request.getParticipantLimit());
        if (request.getRequestModeration() != null) event.setRequestModeration(request.getRequestModeration());
        if (request.getTitle() != null) event.setTitle(request.getTitle());
    }

    private void updateEventFields(Event event, UpdateEventUserRequest request) {
        if (request.getAnnotation() != null) event.setAnnotation(request.getAnnotation());
        if (request.getCategory() != null) {
            Category category = categoryRepository.findById(request.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category not found"));
            event.setCategory(category);
        }
        if (request.getDescription() != null) event.setDescription(request.getDescription());
        if (request.getEventDate() != null) event.setEventDate(request.getEventDate());
        if (request.getLocation() != null) event.setLocation(request.getLocation());
        if (request.getPaid() != null) event.setPaid(request.getPaid());
        if (request.getParticipantLimit() != null) event.setParticipantLimit(request.getParticipantLimit());
        if (request.getRequestModeration() != null) event.setRequestModeration(request.getRequestModeration());
        if (request.getTitle() != null) event.setTitle(request.getTitle());
    }

    private EndpointHitDto createEndpointHit(HttpServletRequest request) {
        return new EndpointHitDto(
                null,
                "ewm-main-service",
                request.getRequestURI(),
                request.getRemoteAddr(),
                LocalDateTime.now()
        );
    }

    private Map<Long, Long> getConfirmedRequests(List<Long> eventIds) {
        return eventIds.stream()
                .collect(Collectors.toMap(
                        eventId -> eventId,
                        eventId -> requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED)
                ));
    }

    private Long getViews(Long eventId) {
        List<ViewStatsDto> responseBody = statsClient.getStats(
                LocalDateTime.of(2000, 1, 1, 0, 0),
                LocalDateTime.of(2100, 1, 1, 0, 0),
                List.of("/events/" + eventId),
                true
        ).getBody();

        if (responseBody != null && !responseBody.isEmpty()) {
            return responseBody.getFirst().getHits();
        }
        return 0L;
    }

    private Map<Long, Long> getViews(List<Long> eventIds) {
        List<String> uris = eventIds.stream()
                .map(id -> String.format("/events/%s", id))
                .toList();
        List<ViewStatsDto> responseBody = statsClient.getStats(
                LocalDateTime.of(2000, 1, 1, 0, 0),
                LocalDateTime.of(2100, 1, 1, 0, 0),
                uris,
                true
        ).getBody();

        Map<Long, Long> viewsMap = new HashMap<>();
        if (responseBody != null) {
            viewsMap = responseBody.stream()
                    .filter(it -> it.getUri().startsWith("/events/"))
                    .collect(Collectors.toMap(
                            it -> Long.parseLong(it.getUri().substring("/events/".length())), ViewStatsDto::getHits)
                    );
        }

        for (Long eventId : eventIds) {
            viewsMap.putIfAbsent(eventId, 0L);
        }

        return viewsMap;
    }
}
