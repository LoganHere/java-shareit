package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.shareit.IntegrationTestBase;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ItemServiceImplIntegrationTest extends IntegrationTestBase {
    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private ItemRequestService itemRequestService;

    private UserDto createUser(String email) {
        return userService.create(UserDto.builder().name("User").email(email).build());
    }

    private ItemDto createItem(long ownerId, String name) {
        return itemService.create(ownerId, ItemDto.builder()
                .name(name)
                .description("desc")
                .available(true)
                .build());
    }

    @Test
    void createAndGetById() {
        UserDto owner = createUser("owner1@mail.ru");
        ItemDto item = createItem(owner.getId(), "Drill");
        assertThat(itemService.getById(item.getId()).getName()).isEqualTo("Drill");
    }

    @Test
    void createWithRequestId() {
        UserDto owner = createUser("owner_req@mail.ru");
        UserDto requestor = createUser("requestor_req@mail.ru");
        ItemRequestResponseDto request = itemRequestService.create(requestor.getId(),
                ItemRequestDto.builder().description("Need drill").build());

        ItemDto item = itemService.create(owner.getId(), ItemDto.builder()
                .name("Drill")
                .description("desc")
                .available(true)
                .requestId(request.getId())
                .build());

        assertThat(item.getRequestId()).isEqualTo(request.getId());
    }

    @Test
    void updateItem() {
        UserDto owner = createUser("owner2@mail.ru");
        ItemDto item = createItem(owner.getId(), "Drill");
        ItemDto updated = itemService.update(owner.getId(), item.getId(),
                UpdateItemDto.builder().name("Saw").available(false).build());
        assertThat(updated.getName()).isEqualTo("Saw");
        assertThat(updated.getAvailable()).isFalse();
    }

    @Test
    void updateByNonOwner() {
        UserDto owner = createUser("owner3@mail.ru");
        UserDto stranger = createUser("stranger3@mail.ru");
        ItemDto item = createItem(owner.getId(), "Drill");

        assertThatThrownBy(() -> itemService.update(stranger.getId(), item.getId(),
                UpdateItemDto.builder().name("Saw").build()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateWithBlankName() {
        UserDto owner = createUser("owner4@mail.ru");
        ItemDto item = createItem(owner.getId(), "Drill");

        assertThatThrownBy(() -> itemService.update(owner.getId(), item.getId(),
                UpdateItemDto.builder().name("  ").build()))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void updateWithBlankDescription() {
        UserDto owner = createUser("owner5@mail.ru");
        ItemDto item = createItem(owner.getId(), "Drill");

        assertThatThrownBy(() -> itemService.update(owner.getId(), item.getId(),
                UpdateItemDto.builder().description("  ").build()))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void searchReturnsOnlyAvailable() {
        UserDto owner = createUser("owner6@mail.ru");
        createItem(owner.getId(), "Hammer");
        itemService.create(owner.getId(), ItemDto.builder()
                .name("Hammer2").description("desc").available(false).build());
        assertThat(itemService.search("Hammer")).hasSize(1);
    }

    @Test
    void searchWithBlankText() {
        assertThat(itemService.search("  ")).isEmpty();
    }

    @Test
    void getByIdNotFound() {
        assertThatThrownBy(() -> itemService.getById(999999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void addCommentAfterCompletedBooking() {
        UserDto owner = createUser("owner7@mail.ru");
        UserDto booker = createUser("booker7@mail.ru");
        ItemDto item = createItem(owner.getId(), "Drill");

        BookingRequestDto bookingDto = BookingRequestDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().minusDays(1))
                .build();
        var booking = bookingService.create(booker.getId(), bookingDto);
        bookingService.approve(owner.getId(), booking.getId(), true);

        CommentDto comment = itemService.addComment(booker.getId(), item.getId(),
                CommentDto.builder().text("Great").build());
        assertThat(comment.getText()).isEqualTo("Great");
    }

    @Test
    void addCommentWithoutBooking() {
        UserDto owner = createUser("owner8@mail.ru");
        UserDto stranger = createUser("stranger8@mail.ru");
        ItemDto item = createItem(owner.getId(), "Drill");

        assertThatThrownBy(() -> itemService.addComment(stranger.getId(), item.getId(),
                CommentDto.builder().text("Nice").build()))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void getByOwnerReturnsItems() {
        UserDto owner = createUser("owner9@mail.ru");
        createItem(owner.getId(), "A");
        createItem(owner.getId(), "B");
        assertThat(itemService.getByOwner(owner.getId())).hasSize(2);
    }

    @Test
    void getByOwnerEmpty() {
        UserDto owner = createUser("owner10@mail.ru");
        assertThat(itemService.getByOwner(owner.getId())).isEmpty();
    }
}