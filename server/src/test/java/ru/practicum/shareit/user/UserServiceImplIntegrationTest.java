package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.shareit.IntegrationTestBase;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
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
    void createWithInvalidEmail() {
        assertThatThrownBy(() -> userService.create(UserDto.builder().name("Ivan").email("invalid").build()))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void createWithBlankEmail() {
        assertThatThrownBy(() -> userService.create(UserDto.builder().name("Ivan").email("   ").build()))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void updateName() {
        UserDto created = userService.create(UserDto.builder().name("Ivan").email("upd@mail.ru").build());
        UserDto updated = userService.update(created.getId(), UpdateUserDto.builder().name("Petr").build());
        assertThat(updated.getName()).isEqualTo("Petr");
    }

    @Test
    void updateEmail() {
        UserDto created = userService.create(UserDto.builder().name("Ivan").email("upd2@mail.ru").build());
        UserDto updated = userService.update(created.getId(), UpdateUserDto.builder().email("new@mail.ru").build());
        assertThat(updated.getEmail()).isEqualTo("new@mail.ru");
    }

    @Test
    void updateEmailToSame() {
        UserDto created = userService.create(UserDto.builder().name("Ivan").email("same@mail.ru").build());
        UserDto updated = userService.update(created.getId(), UpdateUserDto.builder().email("same@mail.ru").build());
        assertThat(updated.getEmail()).isEqualTo("same@mail.ru");
    }

    @Test
    void updateWithDuplicateEmail() {
        userService.create(UserDto.builder().name("A").email("a1@mail.ru").build());
        UserDto b = userService.create(UserDto.builder().name("B").email("b1@mail.ru").build());

        assertThatThrownBy(() -> userService.update(b.getId(), UpdateUserDto.builder().email("a1@mail.ru").build()))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void updateWithBlankName() {
        UserDto created = userService.create(UserDto.builder().name("Ivan").email("blankname@mail.ru").build());
        assertThatThrownBy(() -> userService.update(created.getId(), UpdateUserDto.builder().name("   ").build()))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void updateWithInvalidEmail() {
        UserDto created = userService.create(UserDto.builder().name("Ivan").email("inved@mail.ru").build());
        assertThatThrownBy(() -> userService.update(created.getId(), UpdateUserDto.builder().email("bad").build()))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void getByIdNotFound() {
        assertThatThrownBy(() -> userService.getById(999999L))
                .isInstanceOf(NotFoundException.class);
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
        userService.create(UserDto.builder().name("A").email("ga@mail.ru").build());
        userService.create(UserDto.builder().name("B").email("gb@mail.ru").build());
        assertThat(userService.getAll()).hasSize(2);
    }
}