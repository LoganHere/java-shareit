package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDto create(UserDto dto) {
        validateEmail(dto.getEmail());
        checkEmailUnique(dto.getEmail(), null);
        return UserMapper.toUserDto(userRepository.save(UserMapper.toUser(dto)));
    }

    @Override
    @Transactional
    public UserDto update(long userId, UpdateUserDto dto) {
        User user = getUser(userId);
        if (dto.getName() != null) {
            if (dto.getName().isBlank()) {
                throw new ValidationException("Имя пользователя не должно быть пустым");
            }
            user.setName(dto.getName());
        }
        if (dto.getEmail() != null) {
            validateEmail(dto.getEmail());
            checkEmailUnique(dto.getEmail(), userId);
            user.setEmail(dto.getEmail());
        }
        return UserMapper.toUserDto(userRepository.save(user));
    }

    @Override
    public UserDto getById(long userId) {
        return UserMapper.toUserDto(getUser(userId));
    }

    @Override
    public List<UserDto> getAll() {
        return userRepository.findAll().stream().map(UserMapper::toUserDto).toList();
    }

    @Override
    @Transactional
    public void delete(long userId) {
        userRepository.deleteById(userId);
    }

    private User getUser(long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден"));
    }

    private void checkEmailUnique(String email, Long excludedId) {
        if (userRepository.existsByEmail(email)) {
            if (excludedId == null) {
                throw new ConflictException("Пользователь с email " + email + " уже существует");
            }
            User existingUser = userRepository.findAll().stream()
                    .filter(u -> u.getEmail().equalsIgnoreCase(email))
                    .findFirst().orElse(null);
            if (existingUser != null && !existingUser.getId().equals(excludedId)) {
                throw new ConflictException("Пользователь с email " + email + " уже существует");
            }
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new ValidationException("Email должен быть непустым и содержать символ @");
        }
    }
}