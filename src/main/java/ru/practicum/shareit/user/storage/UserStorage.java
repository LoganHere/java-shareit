package ru.practicum.shareit.user.storage;

import ru.practicum.shareit.user.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {
    User create(User user);

    User update(User user);

    Optional<User> findById(long id);

    List<User> findAll();

    void deleteById(long id);

    boolean existsByEmail(String email, Long excludedUserId);
}
