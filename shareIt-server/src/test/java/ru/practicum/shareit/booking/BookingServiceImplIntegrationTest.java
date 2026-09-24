package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.shareit.IntegrationTestBase;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
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

    private ItemDto createItem(long ownerId, boolean available) {
        return itemService.create(ownerId, ItemDto.builder()
                .name("Drill").description("desc").available(available).build());
    }

    private BookingRequestDto bookingRequest(long itemId, LocalDateTime start, LocalDateTime end) {
        return BookingRequestDto.builder().itemId(itemId).start(start).end(end).build();
    }

    @Test
    void createBooking() {
        UserDto owner = createUser("b1owner@mail.ru");
        UserDto booker = createUser("b1booker@mail.ru");
        ItemDto item = createItem(owner.getId(), true);

        BookingDto booking = bookingService.create(booker.getId(), bookingRequest(
                item.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)));

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.WAITING);
    }

    @Test
    void createBookingForUnavailableItem() {
        UserDto owner = createUser("b2owner@mail.ru");
        UserDto booker = createUser("b2booker@mail.ru");
        ItemDto item = createItem(owner.getId(), false);

        assertThatThrownBy(() -> bookingService.create(booker.getId(), bookingRequest(
                item.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2))))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void ownerCannotBookOwnItem() {
        UserDto owner = createUser("b3owner@mail.ru");
        ItemDto item = createItem(owner.getId(), true);

        assertThatThrownBy(() -> bookingService.create(owner.getId(), bookingRequest(
                item.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2))))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void createWithEndBeforeStart() {
        UserDto owner = createUser("b4owner@mail.ru");
        UserDto booker = createUser("b4booker@mail.ru");
        ItemDto item = createItem(owner.getId(), true);

        assertThatThrownBy(() -> bookingService.create(booker.getId(), bookingRequest(
                item.getId(),
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(1))))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void createWithNonExistentItem() {
        UserDto booker = createUser("b5booker@mail.ru");

        assertThatThrownBy(() -> bookingService.create(booker.getId(), bookingRequest(
                999999L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2))))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void approveByOwner() {
        UserDto owner = createUser("b6owner@mail.ru");
        UserDto booker = createUser("b6booker@mail.ru");
        ItemDto item = createItem(owner.getId(), true);

        BookingDto booking = bookingService.create(booker.getId(), bookingRequest(
                item.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)));

        BookingDto approved = bookingService.approve(owner.getId(), booking.getId(), true);
        assertThat(approved.getStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    void approveByNonOwner() {
        UserDto owner = createUser("b7owner@mail.ru");
        UserDto booker = createUser("b7booker@mail.ru");
        UserDto stranger = createUser("b7stranger@mail.ru");
        ItemDto item = createItem(owner.getId(), true);

        BookingDto booking = bookingService.create(booker.getId(), bookingRequest(
                item.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)));

        assertThatThrownBy(() -> bookingService.approve(stranger.getId(), booking.getId(), true))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void approveAlreadyProcessed() {
        UserDto owner = createUser("b8owner@mail.ru");
        UserDto booker = createUser("b8booker@mail.ru");
        ItemDto item = createItem(owner.getId(), true);

        BookingDto booking = bookingService.create(booker.getId(), bookingRequest(
                item.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)));

        bookingService.approve(owner.getId(), booking.getId(), true);

        assertThatThrownBy(() -> bookingService.approve(owner.getId(), booking.getId(), false))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void approveRejected() {
        UserDto owner = createUser("b9owner@mail.ru");
        UserDto booker = createUser("b9booker@mail.ru");
        ItemDto item = createItem(owner.getId(), true);

        BookingDto booking = bookingService.create(booker.getId(), bookingRequest(
                item.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)));

        BookingDto rejected = bookingService.approve(owner.getId(), booking.getId(), false);
        assertThat(rejected.getStatus()).isEqualTo(BookingStatus.REJECTED);
    }

    @Test
    void approveNotFound() {
        UserDto owner = createUser("b10owner@mail.ru");
        assertThatThrownBy(() -> bookingService.approve(owner.getId(), 999999L, true))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getByIdByBooker() {
        UserDto owner = createUser("b11owner@mail.ru");
        UserDto booker = createUser("b11booker@mail.ru");
        ItemDto item = createItem(owner.getId(), true);

        BookingDto booking = bookingService.create(booker.getId(), bookingRequest(
                item.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)));

        assertThat(bookingService.getById(booker.getId(), booking.getId()).getId())
                .isEqualTo(booking.getId());
    }

    @Test
    void getByIdByOwner() {
        UserDto owner = createUser("b12owner@mail.ru");
        UserDto booker = createUser("b12booker@mail.ru");
        ItemDto item = createItem(owner.getId(), true);

        BookingDto booking = bookingService.create(booker.getId(), bookingRequest(
                item.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)));

        assertThat(bookingService.getById(owner.getId(), booking.getId()).getId())
                .isEqualTo(booking.getId());
    }

    @Test
    void getByIdByStranger() {
        UserDto owner = createUser("b13owner@mail.ru");
        UserDto booker = createUser("b13booker@mail.ru");
        UserDto stranger = createUser("b13stranger@mail.ru");
        ItemDto item = createItem(owner.getId(), true);

        BookingDto booking = bookingService.create(booker.getId(), bookingRequest(
                item.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)));

        assertThatThrownBy(() -> bookingService.getById(stranger.getId(), booking.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getByIdNotFound() {
        UserDto user = createUser("b14@mail.ru");
        assertThatThrownBy(() -> bookingService.getById(user.getId(), 999999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getAllByBookerAllStates() {
        UserDto owner = createUser("b15owner@mail.ru");
        UserDto booker = createUser("b15booker@mail.ru");
        ItemDto item = createItem(owner.getId(), true);

        BookingDto booking = bookingService.create(booker.getId(), bookingRequest(
                item.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)));
        bookingService.approve(owner.getId(), booking.getId(), true);

        assertThat(bookingService.getAllByBooker(booker.getId(), "ALL")).hasSize(1);
        assertThat(bookingService.getAllByBooker(booker.getId(), "CURRENT")).isEmpty();
        assertThat(bookingService.getAllByBooker(booker.getId(), "PAST")).isEmpty();
        assertThat(bookingService.getAllByBooker(booker.getId(), "FUTURE")).hasSize(1);
        assertThat(bookingService.getAllByBooker(booker.getId(), "WAITING")).isEmpty();
        assertThat(bookingService.getAllByBooker(booker.getId(), "REJECTED")).isEmpty();
    }

    @Test
    void getAllByBookerRejected() {
        UserDto owner = createUser("b16owner@mail.ru");
        UserDto booker = createUser("b16booker@mail.ru");
        ItemDto item = createItem(owner.getId(), true);

        BookingDto booking = bookingService.create(booker.getId(), bookingRequest(
                item.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)));
        bookingService.approve(owner.getId(), booking.getId(), false);

        assertThat(bookingService.getAllByBooker(booker.getId(), "REJECTED")).hasSize(1);
    }

    @Test
    void getAllByBookerWaiting() {
        UserDto owner = createUser("b17owner@mail.ru");
        UserDto booker = createUser("b17booker@mail.ru");
        ItemDto item = createItem(owner.getId(), true);

        bookingService.create(booker.getId(), bookingRequest(
                item.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)));

        assertThat(bookingService.getAllByBooker(booker.getId(), "WAITING")).hasSize(1);
    }

    @Test
    void getAllByBookerCurrentAndPast() {
        UserDto owner = createUser("b18owner@mail.ru");
        UserDto booker = createUser("b18booker@mail.ru");
        ItemDto item = createItem(owner.getId(), true);

        BookingDto past = bookingService.create(booker.getId(), bookingRequest(
                item.getId(),
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(1)));
        bookingService.approve(owner.getId(), past.getId(), true);

        BookingDto current = bookingService.create(booker.getId(), bookingRequest(
                item.getId(),
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusHours(1)));
        bookingService.approve(owner.getId(), current.getId(), true);

        assertThat(bookingService.getAllByBooker(booker.getId(), "PAST")).hasSize(1);
        assertThat(bookingService.getAllByBooker(booker.getId(), "CURRENT")).hasSize(1);
    }

    @Test
    void getAllByOwnerAllStates() {
        UserDto owner = createUser("b19owner@mail.ru");
        UserDto booker = createUser("b19booker@mail.ru");
        ItemDto item = createItem(owner.getId(), true);

        BookingDto booking = bookingService.create(booker.getId(), bookingRequest(
                item.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)));
        bookingService.approve(owner.getId(), booking.getId(), true);

        assertThat(bookingService.getAllByOwner(owner.getId(), "ALL")).hasSize(1);
        assertThat(bookingService.getAllByOwner(owner.getId(), "FUTURE")).hasSize(1);
        assertThat(bookingService.getAllByOwner(owner.getId(), "WAITING")).isEmpty();
        assertThat(bookingService.getAllByOwner(owner.getId(), "REJECTED")).isEmpty();
    }

    @Test
    void getAllByOwnerRejected() {
        UserDto owner = createUser("b20owner@mail.ru");
        UserDto booker = createUser("b20booker@mail.ru");
        ItemDto item = createItem(owner.getId(), true);

        BookingDto booking = bookingService.create(booker.getId(), bookingRequest(
                item.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)));
        bookingService.approve(owner.getId(), booking.getId(), false);

        assertThat(bookingService.getAllByOwner(owner.getId(), "REJECTED")).hasSize(1);
    }

    @Test
    void getAllByOwnerCurrentAndPast() {
        UserDto owner = createUser("b21owner@mail.ru");
        UserDto booker = createUser("b21booker@mail.ru");
        ItemDto item = createItem(owner.getId(), true);

        BookingDto past = bookingService.create(booker.getId(), bookingRequest(
                item.getId(),
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(1)));
        bookingService.approve(owner.getId(), past.getId(), true);

        BookingDto current = bookingService.create(booker.getId(), bookingRequest(
                item.getId(),
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusHours(1)));
        bookingService.approve(owner.getId(), current.getId(), true);

        assertThat(bookingService.getAllByOwner(owner.getId(), "PAST")).hasSize(1);
        assertThat(bookingService.getAllByOwner(owner.getId(), "CURRENT")).hasSize(1);
    }
}