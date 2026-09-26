package ru.practicum.shareit.request.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.dto.ItemResponseDto;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ItemRequestMapper {
    public static ItemRequest toItemRequest(String description, User requestor) {
        return ItemRequest.builder()
                .description(description)
                .requestor(requestor)
                .created(LocalDateTime.now())
                .build();
    }

    public static ItemRequestResponseDto toItemRequestResponseDto(ItemRequest request, List<Item> items) {
        return ItemRequestResponseDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated())
                .items(items.stream().map(ItemRequestMapper::toItemResponseDto).toList())
                .build();
    }

    private static ItemResponseDto toItemResponseDto(Item item) {
        return ItemResponseDto.builder()
                .id(item.getId())
                .name(item.getName())
                .ownerId(item.getOwner().getId())
                .build();
    }
}