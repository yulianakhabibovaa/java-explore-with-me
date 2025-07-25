package ru.practicum.ewm.subscriptions.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.subscriptions.model.Subscription;

import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    boolean existsBySubscriberIdAndAuthorId(Long subscriberId, Long authorId);

    void deleteBySubscriberIdAndAuthorId(Long subscriberId, Long authorId);

    List<Subscription> findBySubscriberId(Long subscriberId);

    List<Subscription> findAuthorIdsBySubscriberId(Long subscriberId);
}