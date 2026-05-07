package ru.practicum.shareit.user;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    public UserServiceImpl(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDto create(UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        return UserMapper.toDto(repository.save(user));
    }

    @Override
    public UserDto update(long userId, UserDto userDto) {
        User existing = repository.findById(userId);
        if (userDto.getName() != null) {
            existing.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            existing.setEmail(userDto.getEmail());
        }
        return UserMapper.toDto(repository.save(existing));
    }

    @Override
    public UserDto getById(long userId) {
        return UserMapper.toDto(repository.findById(userId));
    }

    @Override
    public List<UserDto> getAll() {
        List<User> users = repository.findAll();
        List<UserDto> result = new ArrayList<>();
        for (User user : users) {
            result.add(UserMapper.toDto(user));
        }
        return result;
    }

    @Override
    public void delete(long userId) {
        repository.deleteById(userId);
    }
}