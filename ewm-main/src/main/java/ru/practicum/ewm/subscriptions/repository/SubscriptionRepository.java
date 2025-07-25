package ru.practicum.ewm.subscriptions.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.subscriptions.model.Subscription;

import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    boolean existsBySubscriberIdAndAuthorId(Long subscriberId, Long authorId);

    void deleteBySubscriberIdAndAuthorId(Long subscriberId, Long authorId);

    List<Subscription> findBySubscriberId(Long subscriberId);

    Page<Subscription> findByAuthorId(Long subscriberId, Pageable pageable);

    List<Subscription> findAuthorIdsBySubscriberId(Long subscriberId);
}