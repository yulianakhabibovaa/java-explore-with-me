package ru.practicum.ewm.subscriptions.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.error.ConflictException;
import ru.practicum.ewm.error.NotFoundException;
import ru.practicum.ewm.error.ValidationException;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.subscriptions.model.Subscription;
import ru.practicum.ewm.subscriptions.repository.SubscriptionRepository;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.mapper.UserMapper;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final EventService eventService;

    @Transactional
    public void subscribe(Long subscriberId, Long authorId) {
        if (!userRepository.existsById(subscriberId)) {
            throw new NotFoundException("User with id " + subscriberId + " not found");
        }
        if (!userRepository.existsById(authorId)) {
            throw new NotFoundException("User with id " + authorId + " not found");
        }
        if (subscriberId.equals(authorId)) {
            throw new ValidationException("Cannot subscribe to yourself");
        }
        if (subscriptionRepository.existsBySubscriberIdAndAuthorId(subscriberId, authorId)) {
            throw new ConflictException("Subscription already exists");
        }

        Subscription subscription = Subscription.builder()
                .subscriber(User.builder().id(subscriberId).build())
                .author(User.builder().id(authorId).build())
                .created(LocalDateTime.now())
                .build();

        subscriptionRepository.save(subscription);
        log.info("Пользователь {} подписался на автора {}", subscriberId, authorId);
    }

    @Transactional
    public void unsubscribe(Long subscriberId, Long authorId) {
        if (!subscriptionRepository.existsBySubscriberIdAndAuthorId(subscriberId, authorId)) {
            throw new NotFoundException("Subscription not found");
        }
        subscriptionRepository.deleteBySubscriberIdAndAuthorId(subscriberId, authorId);
        log.info("Пользователь {} отписался от автора {}", subscriberId, authorId);
    }

    public List<UserDto> getSubscriptions(Long subscriberId) {
        if (!userRepository.existsById(subscriberId)) {
            throw new NotFoundException("User with id " + subscriberId + " not found");
        }

        List<Subscription> subscriptions = subscriptionRepository.findBySubscriberId(subscriberId);
        return UserMapper.toUserDtoList(
                userRepository.findByIdIn(subscriptions.stream()
                .map(sub -> sub.getAuthor().getId())
                .toList())
        );
    }

    public List<EventShortDto> getFeed(Long subscriberId, int from, int size) {
        if (!userRepository.existsById(subscriberId)) {
            throw new NotFoundException("User with id " + subscriberId + " not found");
        }

        List<Long> authorIds = subscriptionRepository.findAuthorIdsBySubscriberId(subscriberId);

        if (authorIds.isEmpty()) {
            return Collections.emptyList();
        }

        return eventService.getPublishedEventsByAuthors(authorIds, from, size);
    }
}
