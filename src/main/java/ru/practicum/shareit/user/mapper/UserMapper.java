package ru.practicum.shareit.user.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserMapper {
    public static UserDto toUserDto(User user) {
        return UserDto.builder().id(user.getId()).name(user.getName()).email(user.getEmail()).build();
    }

    public static User toUser(UserDto dto) {
        return User.builder().id(dto.getId()).name(dto.getName()).email(dto.getEmail()).build();
    }
}
