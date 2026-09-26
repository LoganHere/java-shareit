package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.service.BookingService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    @Test
    void create() throws Exception {
        BookingDto dto = BookingDto.builder().id(1L).status(BookingStatus.WAITING)
                .start(LocalDateTime.now().plusDays(1)).end(LocalDateTime.now().plusDays(2)).build();
        when(bookingService.create(eq(1L), any())).thenReturn(dto);

        BookingRequestDto request = BookingRequestDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    void approve() throws Exception {
        BookingDto dto = BookingDto.builder().id(1L).status(BookingStatus.APPROVED).build();
        when(bookingService.approve(1L, 1L, true)).thenReturn(dto);

        mockMvc.perform(patch("/bookings/1").param("approved", "true")
                        .header(USER_ID_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void getById() throws Exception {
        BookingDto dto = BookingDto.builder().id(1L).status(BookingStatus.WAITING).build();
        when(bookingService.getById(1L, 1L)).thenReturn(dto);

        mockMvc.perform(get("/bookings/1").header(USER_ID_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getAllByBooker() throws Exception {
        when(bookingService.getAllByBooker(1L, "ALL")).thenReturn(List.of(
                BookingDto.builder().id(1L).status(BookingStatus.WAITING).build()));

        mockMvc.perform(get("/bookings").header(USER_ID_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getAllByOwner() throws Exception {
        when(bookingService.getAllByOwner(1L, "ALL")).thenReturn(List.of(
                BookingDto.builder().id(1L).status(BookingStatus.WAITING).build()));

        mockMvc.perform(get("/bookings/owner").header(USER_ID_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }
}