package ru.practicum.shareit.user.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class UserDtoJsonTest {
    @Autowired
    private JacksonTester<UserDto> json;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void serialize() throws Exception {
        UserDto dto = UserDto.builder().id(1L).name("Ivan").email("ivan@mail.ru").build();
        assertThat(json.write(dto)).hasJsonPathStringValue("$.email", "ivan@mail.ru");
    }

    @Test
    void deserialize() throws Exception {
        String content = "{\"id\":1,\"name\":\"Ivan\",\"email\":\"ivan@mail.ru\"}";
        UserDto dto = objectMapper.readValue(content, UserDto.class);
        assertThat(dto.getName()).isEqualTo("Ivan");
        assertThat(dto.getEmail()).isEqualTo("ivan@mail.ru");
    }
}