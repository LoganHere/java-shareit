package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestResponseDto create(long userId, ItemRequestDto dto);

    List<ItemRequestResponseDto> getAllByRequestor(long userId);

    List<ItemRequestResponseDto> getAllByOtherUsers(long userId);

    ItemRequestResponseDto getById(long userId, long requestId);
}