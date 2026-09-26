package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingClient bookingClient;

    @Test
    void create() throws Exception {
        when(bookingClient.create(eq(1L), any())).thenReturn(ResponseEntity.ok(new BookingDto()));
        BookingRequestDto dto = BookingRequestDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void approve() throws Exception {
        when(bookingClient.approve(eq(1L), eq(1L), eq(true))).thenReturn(ResponseEntity.ok(new BookingDto()));
        mockMvc.perform(patch("/bookings/1")
                        .header(USER_ID_HEADER, 1)
                        .param("approved", "true"))
                .andExpect(status().isOk());
    }

    @Test
    void getById() throws Exception {
        when(bookingClient.getById(anyLong(), anyLong())).thenReturn(ResponseEntity.ok(new BookingDto()));
        mockMvc.perform(get("/bookings/1").header(USER_ID_HEADER, 1)).andExpect(status().isOk());
    }

    @Test
    void getAllByBooker() throws Exception {
        when(bookingClient.getAllByBooker(1L, "ALL")).thenReturn(ResponseEntity.ok(List.of()));
        mockMvc.perform(get("/bookings").header(USER_ID_HEADER, 1)).andExpect(status().isOk());
    }
}