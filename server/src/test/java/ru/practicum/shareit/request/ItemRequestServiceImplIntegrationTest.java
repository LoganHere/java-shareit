package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.shareit.IntegrationTestBase;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ItemRequestServiceImplIntegrationTest extends IntegrationTestBase {
    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    private UserDto createUser(String email) {
        return userService.create(UserDto.builder().name("User").email(email).build());
    }

    @Test
    void createRequest() {
        UserDto user = createUser("r1@mail.ru");
        ItemRequestResponseDto request = itemRequestService.create(user.getId(),
                ItemRequestDto.builder().description("Need a drill").build());
        assertThat(request.getId()).isNotNull();
        assertThat(request.getDescription()).isEqualTo("Need a drill");
    }

    @Test
    void getAllByRequestor() {
        UserDto user = createUser("r2@mail.ru");
        itemRequestService.create(user.getId(), ItemRequestDto.builder().description("A").build());
        itemRequestService.create(user.getId(), ItemRequestDto.builder().description("B").build());
        assertThat(itemRequestService.getAllByRequestor(user.getId())).hasSize(2);
    }

    @Test
    void getAllByRequestorEmpty() {
        UserDto user = createUser("r3@mail.ru");
        assertThat(itemRequestService.getAllByRequestor(user.getId())).isEmpty();
    }

    @Test
    void getAllByOtherUsers() {
        UserDto user1 = createUser("r4@mail.ru");
        UserDto user2 = createUser("r5@mail.ru");
        itemRequestService.create(user1.getId(), ItemRequestDto.builder().description("A").build());
        itemRequestService.create(user2.getId(), ItemRequestDto.builder().description("B").build());
        assertThat(itemRequestService.getAllByOtherUsers(user1.getId())).hasSize(1);
    }

    @Test
    void getAllByOtherUsersEmpty() {
        UserDto user1 = createUser("r6@mail.ru");
        assertThat(itemRequestService.getAllByOtherUsers(user1.getId())).isEmpty();
    }

    @Test
    void getByIdWithItems() {
        UserDto requestor = createUser("r7@mail.ru");
        UserDto owner = createUser("r8@mail.ru");

        ItemRequestResponseDto request = itemRequestService.create(requestor.getId(),
                ItemRequestDto.builder().description("Need a drill").build());

        itemService.create(owner.getId(), ItemDto.builder()
                .name("Drill").description("desc").available(true)
                .requestId(request.getId()).build());

        ItemRequestResponseDto found = itemRequestService.getById(requestor.getId(), request.getId());
        assertThat(found.getItems()).hasSize(1);
        assertThat(found.getItems().get(0).getName()).isEqualTo("Drill");
    }

    @Test
    void getByIdWithoutItems() {
        UserDto requestor = createUser("r9@mail.ru");
        ItemRequestResponseDto request = itemRequestService.create(requestor.getId(),
                ItemRequestDto.builder().description("Need a drill").build());

        ItemRequestResponseDto found = itemRequestService.getById(requestor.getId(), request.getId());
        assertThat(found.getItems()).isEmpty();
    }

    @Test
    void getByIdNotFound() {
        UserDto user = createUser("r10@mail.ru");
        assertThatThrownBy(() -> itemRequestService.getById(user.getId(), 999999L))
                .isInstanceOf(NotFoundException.class);
    }
}