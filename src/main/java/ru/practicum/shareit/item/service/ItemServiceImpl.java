package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    @Override
    public ItemDto create(long userId, ItemDto dto) {
        checkUser(userId);
        return ItemMapper.toItemDto(itemStorage.create(ItemMapper.toItem(dto, getUser(userId))));
    }

    @Override
    public ItemDto update(long userId, long itemId, UpdateItemDto dto) {
        checkUser(userId);
        Item item = getItem(itemId);
        if (item.getOwner().getId() != userId) {
            throw new NotFoundException("Вещь с id=" + itemId + " не принадлежит пользователю");
        }
        if (dto.getName() != null) {
            if (dto.getName().isBlank()) {
                throw new ValidationException("Название вещи не должно быть пустым");
            }
            item.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            if (dto.getDescription().isBlank()) {
                throw new ValidationException("Описание вещи не должно быть пустым");
            }
            item.setDescription(dto.getDescription());
        }
        if (dto.getAvailable() != null) {
            item.setAvailable(dto.getAvailable());
        }
        return ItemMapper.toItemDto(itemStorage.update(item));
    }

    @Override
    public ItemDto getById(long itemId) {
        return ItemMapper.toItemDto(getItem(itemId));
    }

    @Override
    public List<ItemDto> getByOwner(long userId) {
        checkUser(userId);
        return itemStorage.findByOwnerId(userId).stream().map(ItemMapper::toItemDto).toList();
    }

    @Override
    public List<ItemDto> search(String text) {
        return itemStorage.searchAvailable(text).stream().map(ItemMapper::toItemDto).toList();
    }

    private Item getItem(long id) {
        return itemStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + id + " не найдена"));
    }

    private User getUser(long id) {
        return userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден"));
    }

    private void checkUser(long id) {
        getUser(id);
    }
}
