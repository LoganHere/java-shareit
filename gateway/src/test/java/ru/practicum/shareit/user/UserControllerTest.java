package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserClient userClient;

    @Test
    void create() throws Exception {
        when(userClient.create(any())).thenReturn(ResponseEntity.ok(new UserDto()));
        UserDto dto = UserDto.builder().name("Ivan").email("ivan@mail.ru").build();
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void update() throws Exception {
        when(userClient.update(eq(1L), any())).thenReturn(ResponseEntity.ok(new UserDto()));
        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UpdateUserDto.builder().name("Petr").build())))
                .andExpect(status().isOk());
    }

    @Test
    void getById() throws Exception {
        when(userClient.getById(1L)).thenReturn(ResponseEntity.ok(new UserDto()));
        mockMvc.perform(get("/users/1")).andExpect(status().isOk());
    }

    @Test
    void getAll() throws Exception {
        when(userClient.getAll()).thenReturn(ResponseEntity.ok(List.of()));
        mockMvc.perform(get("/users")).andExpect(status().isOk());
    }

    @Test
    void deleteUser() throws Exception {
        when(userClient.delete(1L)).thenReturn(ResponseEntity.ok().build());
        mockMvc.perform(delete("/users/1")).andExpect(status().isOk());
    }
}