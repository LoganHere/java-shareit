package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;

import java.util.List;

public interface ItemService {
    ItemDto create(long userId, ItemDto itemDto);

    ItemDto update(long userId, long itemId, UpdateItemDto itemDto);

    ItemDto getById(long itemId);

    List<ItemDto> getByOwner(long userId);

    List<ItemDto> search(String text);

    CommentDto addComment(long userId, long itemId, CommentDto dto);
}