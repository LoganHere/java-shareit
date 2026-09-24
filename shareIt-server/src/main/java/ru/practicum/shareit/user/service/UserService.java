package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto create(UserDto userDto);

    UserDto update(long userId, UpdateUserDto userDto);

    UserDto getById(long userId);

    List<UserDto> getAll();

    void delete(long userId);
}
