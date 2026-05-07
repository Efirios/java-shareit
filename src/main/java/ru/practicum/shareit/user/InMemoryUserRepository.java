package ru.practicum.shareit.user;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class InMemoryUserRepository implements UserRepository {
    private final Map<Long, User> users = new HashMap<>();
    private long nextId = 1;

    @Override
    public User save(User user) {
        if (user.getEmail() != null) {
            for (User existing : users.values()) {
                if (existing.getEmail() != null
                        && existing.getEmail().equals(user.getEmail())
                        && (user.getId() == null || !existing.getId().equals(user.getId()))) {
                    throw new IllegalArgumentException("Email already exists");
                }
            }
        }

        if (user.getId() == null) {
            user.setId(nextId++);
        }
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User findById(long userId) {
        User user = users.get(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        return user;
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void deleteById(long userId) {
        users.remove(userId);
    }
}