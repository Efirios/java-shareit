package ru.practicum.shareit.user;

import java.util.List;

public interface UserRepository {
    User save(User user);

    User findById(long userId);

    List<User> findAll();

    void deleteById(long userId);
}