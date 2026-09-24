package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.shareit.IntegrationTestBase;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserServiceImplIntegrationTest extends IntegrationTestBase {
    @Autowired
    private UserService userService;

    @Test
    void createAndGetById() {
        UserDto created = userService.create(UserDto.builder().name("Ivan").email("ivan@mail.ru").build());
        assertThat(created.getId()).isNotNull();
        assertThat(userService.getById(created.getId()).getEmail()).isEqualTo("ivan@mail.ru");
    }

    @Test
    void createWithDuplicateEmail() {
        userService.create(UserDto.builder().name("Ivan").email("dup@mail.ru").build());
        assertThatThrownBy(() -> userService.create(UserDto.builder().name("Petr").email("dup@mail.ru").build()))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void updateName() {
        UserDto created = userService.create(UserDto.builder().name("Ivan").email("upd@mail.ru").build());
        UserDto updated = userService.update(created.getId(), UpdateUserDto.builder().name("Petr").build());
        assertThat(updated.getName()).isEqualTo("Petr");
    }

    @Test
    void deleteUser() {
        UserDto created = userService.create(UserDto.builder().name("Ivan").email("del@mail.ru").build());
        userService.delete(created.getId());
        assertThatThrownBy(() -> userService.getById(created.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getAll() {
        userService.create(UserDto.builder().name("A").email("a@mail.ru").build());
        userService.create(UserDto.builder().name("B").email("b@mail.ru").build());
        assertThat(userService.getAll()).hasSize(2);
    }
}