package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingRequestDtoJsonTest {
    @Autowired
    private JacksonTester<BookingRequestDto> json;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void serialize() throws Exception {
        BookingRequestDto dto = BookingRequestDto.builder()
                .itemId(1L)
                .start(LocalDateTime.of(2030, 1, 1, 10, 0))
                .end(LocalDateTime.of(2030, 1, 2, 10, 0))
                .build();

        assertThat(json.write(dto)).hasJsonPathNumberValue("$.itemId", 1);
        assertThat(json.write(dto)).hasJsonPathStringValue("$.start", "2030-01-01T10:00:00");
    }

    @Test
    void deserialize() throws Exception {
        String content = "{\"itemId\":1,\"start\":\"2030-01-01T10:00:00\",\"end\":\"2030-01-02T10:00:00\"}";
        BookingRequestDto dto = objectMapper.readValue(content, BookingRequestDto.class);
        assertThat(dto.getItemId()).isEqualTo(1L);
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2030, 1, 1, 10, 0));
    }
}