package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.shareit.IntegrationTestBase;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ItemServiceImplIntegrationTest extends IntegrationTestBase {
    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Autowired
    private BookingService bookingService;

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
    void updateItem() {
        UserDto owner = createUser("owner2@mail.ru");
        ItemDto item = createItem(owner.getId(), "Drill");
        ItemDto updated = itemService.update(owner.getId(), item.getId(),
                UpdateItemDto.builder().name("Saw").available(false).build());
        assertThat(updated.getName()).isEqualTo("Saw");
        assertThat(updated.getAvailable()).isFalse();
    }

    @Test
    void searchReturnsOnlyAvailable() {
        UserDto owner = createUser("owner3@mail.ru");
        createItem(owner.getId(), "Hammer");
        itemService.create(owner.getId(), ItemDto.builder()
                .name("Hammer2").description("desc").available(false).build());
        assertThat(itemService.search("Hammer")).hasSize(1);
    }

    @Test
    void addCommentAfterCompletedBooking() {
        UserDto owner = createUser("owner4@mail.ru");
        UserDto booker = createUser("booker4@mail.ru");
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
    void getByOwnerReturnsItems() {
        UserDto owner = createUser("owner5@mail.ru");
        createItem(owner.getId(), "A");
        createItem(owner.getId(), "B");
        assertThat(itemService.getByOwner(owner.getId())).hasSize(2);
    }
}