package ru.practicum.ewm.request.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.model.ParticipationRequest;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class RequestMapper {

    public ParticipationRequestDto toDto(ParticipationRequest request) {
        return ParticipationRequestDto.builder()
                .id(request.getId())
                .created(request.getCreated())
                .event(request.getEvent().getId())
                .requester(request.getRequester().getId())
                .status(request.getStatus())
                .build();
    }

    public List<ParticipationRequestDto> toDtoList(Collection<ParticipationRequest> requests) {
        return requests.stream()
                .map(RequestMapper::toDto)
                .collect(Collectors.toList());
    }
}
