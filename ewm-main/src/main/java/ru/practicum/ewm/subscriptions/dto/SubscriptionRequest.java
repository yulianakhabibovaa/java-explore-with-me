package ru.practicum.ewm.subscriptions.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionRequest {
    @NotNull(message = "Author ID cannot be null")
    private Long authorId;
}
