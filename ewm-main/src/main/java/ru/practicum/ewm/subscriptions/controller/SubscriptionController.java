package ru.practicum.ewm.subscriptions.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.subscriptions.dto.SubscriptionRequest;
import ru.practicum.ewm.subscriptions.service.SubscriptionService;
import ru.practicum.ewm.user.dto.UserDto;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/subscriptions")
@Validated
public class SubscriptionController {
    private final SubscriptionService subscriptionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void subscribe(
            @PathVariable Long userId,
            @Valid @RequestBody SubscriptionRequest request) {
        subscriptionService.subscribe(userId, request.getAuthorId());
    }

    @DeleteMapping("/{authorId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unsubscribe(
            @PathVariable Long userId,
            @PathVariable Long authorId) {
        subscriptionService.unsubscribe(userId, authorId);
    }

    @GetMapping
    public List<UserDto> getSubscriptions(@PathVariable Long userId) {
        return subscriptionService.getSubscriptions(userId);
    }

    @GetMapping("/feed")
    public List<EventShortDto> getFeed(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") @Min(0) Integer from,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer size) {
        return subscriptionService.getFeed(userId, from, size);
    }
}
