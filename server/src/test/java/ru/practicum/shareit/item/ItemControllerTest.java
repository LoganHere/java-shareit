package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    @Test
    void create() throws Exception {
        ItemDto dto = ItemDto.builder().id(1L).name("Drill").description("desc").available(true).build();
        when(itemService.create(eq(1L), any())).thenReturn(dto);

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void update() throws Exception {
        ItemDto dto = ItemDto.builder().id(1L).name("Saw").description("desc").available(false).build();
        when(itemService.update(eq(1L), eq(1L), any())).thenReturn(dto);

        mockMvc.perform(patch("/items/1")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UpdateItemDto.builder().name("Saw").build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Saw"));
    }

    @Test
    void getById() throws Exception {
        ItemDto dto = ItemDto.builder().id(1L).name("Drill").description("desc").available(true).build();
        when(itemService.getById(1L)).thenReturn(dto);

        mockMvc.perform(get("/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getByOwner() throws Exception {
        when(itemService.getByOwner(1L)).thenReturn(List.of(
                ItemDto.builder().id(1L).name("Drill").description("desc").available(true).build()));

        mockMvc.perform(get("/items").header(USER_ID_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void search() throws Exception {
        when(itemService.search("drill")).thenReturn(List.of(
                ItemDto.builder().id(1L).name("Drill").description("desc").available(true).build()));

        mockMvc.perform(get("/items/search").param("text", "drill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Drill"));
    }

    @Test
    void addComment() throws Exception {
        CommentDto dto = CommentDto.builder().id(1L).text("Great").authorName("Ivan").build();
        when(itemService.addComment(eq(1L), eq(1L), any())).thenReturn(dto);

        mockMvc.perform(post("/items/1/comment")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CommentDto.builder().text("Great").build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Great"));
    }
}