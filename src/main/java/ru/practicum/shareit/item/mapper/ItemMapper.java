package ru.practicum.shareit.item.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ItemMapper {
    public static ItemDto toItemDto(Item item) {
        Long requestId = item.getRequest() == null ? null : item.getRequest().getId();
        return ItemDto.builder().id(item.getId()).name(item.getName())
                .description(item.getDescription()).available(item.getAvailable())
                .requestId(requestId).build();
    }

    public static Item toItem(ItemDto dto, User owner) {
        ItemRequest request = dto.getRequestId() == null
                ? null : ItemRequest.builder().id(dto.getRequestId()).build();
        return Item.builder().id(dto.getId()).name(dto.getName()).description(dto.getDescription())
                .available(dto.getAvailable()).owner(owner).request(request).build();
    }
}
