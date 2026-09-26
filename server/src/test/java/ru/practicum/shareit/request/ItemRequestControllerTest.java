package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService itemRequestService;

    @Test
    void create() throws Exception {
        ItemRequestResponseDto dto = ItemRequestResponseDto.builder()
                .id(1L).description("Need drill").created(LocalDateTime.now()).items(List.of()).build();
        when(itemRequestService.create(eq(1L), any())).thenReturn(dto);

        mockMvc.perform(post("/requests")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ItemRequestDto.builder().description("Need drill").build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getAllByRequestor() throws Exception {
        when(itemRequestService.getAllByRequestor(1L)).thenReturn(List.of(
                ItemRequestResponseDto.builder().id(1L).description("A").created(LocalDateTime.now()).items(List.of()).build()));

        mockMvc.perform(get("/requests").header(USER_ID_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getAllByOtherUsers() throws Exception {
        when(itemRequestService.getAllByOtherUsers(1L)).thenReturn(List.of(
                ItemRequestResponseDto.builder().id(1L).description("A").created(LocalDateTime.now()).items(List.of()).build()));

        mockMvc.perform(get("/requests/all").header(USER_ID_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getById() throws Exception {
        when(itemRequestService.getById(1L, 1L)).thenReturn(
                ItemRequestResponseDto.builder().id(1L).description("A").created(LocalDateTime.now()).items(List.of()).build());

        mockMvc.perform(get("/requests/1").header(USER_ID_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }
}