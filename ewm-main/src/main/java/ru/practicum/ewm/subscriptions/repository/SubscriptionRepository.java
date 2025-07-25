package ru.practicum.ewm.subscriptions.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.subscriptions.model.Subscription;

import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    boolean existsBySubscriberIdAndAuthorId(Long subscriberId, Long authorId);

    void deleteBySubscriberIdAndAuthorId(Long subscriberId, Long authorId);

    List<Subscription> findBySubscriberId(Long subscriberId);

    @Query("SELECT s.author.id FROM Subscription s WHERE s.subscriber.id = :subscriberId")
    List<Long> findAuthorIdsBySubscriberId(Long subscriberId);
}