package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.service.UserService;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class ErrorHandlerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void handleNotFound() throws Exception {
        when(userService.getById(anyLong())).thenThrow(new NotFoundException("Не найдено"));
        mockMvc.perform(get("/users/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Не найдено"));
    }

    @Test
    void handleValidation() throws Exception {
        when(userService.getById(anyLong())).thenThrow(new ValidationException("Некорректно"));
        mockMvc.perform(get("/users/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Некорректно"));
    }

    @Test
    void handleConflict() throws Exception {
        when(userService.getById(anyLong())).thenThrow(new ConflictException("Конфликт"));
        mockMvc.perform(get("/users/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Конфликт"));
    }

    @Test
    void handleForbidden() throws Exception {
        when(userService.getById(anyLong())).thenThrow(new ForbiddenException("Запрещено"));
        mockMvc.perform(get("/users/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Запрещено"));
    }
}