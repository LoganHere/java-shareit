package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemClient itemClient;

    @Test
    void create() throws Exception {
        when(itemClient.create(eq(1L), any())).thenReturn(ResponseEntity.ok(new ItemDto()));
        ItemDto dto = ItemDto.builder().name("Drill").description("desc").available(true).build();
        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void update() throws Exception {
        when(itemClient.update(eq(1L), eq(1L), any())).thenReturn(ResponseEntity.ok(new ItemDto()));
        mockMvc.perform(patch("/items/1")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UpdateItemDto.builder().name("Saw").build())))
                .andExpect(status().isOk());
    }

    @Test
    void getById() throws Exception {
        when(itemClient.getById(anyLong(), anyLong())).thenReturn(ResponseEntity.ok(new ItemDto()));
        mockMvc.perform(get("/items/1").header(USER_ID_HEADER, 1)).andExpect(status().isOk());
    }

    @Test
    void getByOwner() throws Exception {
        when(itemClient.getByOwner(1L)).thenReturn(ResponseEntity.ok(List.of()));
        mockMvc.perform(get("/items").header(USER_ID_HEADER, 1)).andExpect(status().isOk());
    }

    @Test
    void search() throws Exception {
        when(itemClient.search("drill")).thenReturn(ResponseEntity.ok(List.of()));
        mockMvc.perform(get("/items/search").param("text", "drill")).andExpect(status().isOk());
    }

    @Test
    void addComment() throws Exception {
        when(itemClient.addComment(eq(1L), eq(1L), any())).thenReturn(ResponseEntity.ok(new CommentDto()));
        mockMvc.perform(post("/items/1/comment")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CommentDto.builder().text("Great").build())))
                .andExpect(status().isOk());
    }
}