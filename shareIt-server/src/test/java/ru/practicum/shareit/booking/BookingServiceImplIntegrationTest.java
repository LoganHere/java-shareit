package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.shareit.IntegrationTestBase;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookingServiceImplIntegrationTest extends IntegrationTestBase {
    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    private UserDto createUser(String email) {
        return userService.create(UserDto.builder().name("User").email(email).build());
    }

    private ItemDto createItem(long ownerId) {
        return itemService.create(ownerId, ItemDto.builder()
                .name("Drill").description("desc").available(true).build());
    }

    @Test
    void createBooking() {
        UserDto owner = createUser("b1owner@mail.ru");
        UserDto booker = createUser("b1booker@mail.ru");
        ItemDto item = createItem(owner.getId());

        BookingDto booking = bookingService.create(booker.getId(), BookingRequestDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusSeconds(1))
                .end(LocalDateTime.now().plusSeconds(10))
                .build());

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.WAITING);
    }

    @Test
    void ownerCannotBookOwnItem() {
        UserDto owner = createUser("b2owner@mail.ru");
        ItemDto item = createItem(owner.getId());

        assertThatThrownBy(() -> bookingService.create(owner.getId(), BookingRequestDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusSeconds(1))
                .end(LocalDateTime.now().plusSeconds(10))
                .build()))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void approveByNonOwner() {
        UserDto owner = createUser("b3owner@mail.ru");
        UserDto booker = createUser("b3booker@mail.ru");
        UserDto stranger = createUser("b3stranger@mail.ru");
        ItemDto item = createItem(owner.getId());

        BookingDto booking = bookingService.create(booker.getId(), BookingRequestDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusSeconds(1))
                .end(LocalDateTime.now().plusSeconds(10))
                .build());

        assertThatThrownBy(() -> bookingService.approve(stranger.getId(), booking.getId(), true))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void getAllByBooker() {
        UserDto owner = createUser("b4owner@mail.ru");
        UserDto booker = createUser("b4booker@mail.ru");
        ItemDto item = createItem(owner.getId());

        bookingService.create(booker.getId(), BookingRequestDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusSeconds(1))
                .end(LocalDateTime.now().plusSeconds(10))
                .build());

        assertThat(bookingService.getAllByBooker(booker.getId(), "ALL")).hasSize(1);
    }
}