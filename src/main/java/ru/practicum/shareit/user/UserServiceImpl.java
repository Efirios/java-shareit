package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ConflictException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    @Override
    public UserDto create(UserDto userDto) {
        if (userDto.getEmail() != null) {
            checkEmailUnique(null, userDto.getEmail());
        }
        User user = UserMapper.toUser(userDto);
        return UserMapper.toDto(repository.save(user));
    }

    @Override
    public UserDto update(long userId, UserDto userDto) {
        User existing = repository.findById(userId);

        if (userDto.getName() != null) {
            if (userDto.getName().isBlank()) {
                throw new BadRequestException("Name is blank");
            }
            existing.setName(userDto.getName());
        }

        if (userDto.getEmail() != null) {
            if (userDto.getEmail().isBlank()) {
                throw new BadRequestException("Email is blank");
            }
            checkEmailUnique(userId, userDto.getEmail());
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
        return repository.findAll().stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(long userId) {
        repository.findById(userId);
        repository.deleteById(userId);
    }

    private void checkEmailUnique(Long userId, String email) {
        boolean exists = repository.findAll().stream()
                .anyMatch(user -> user.getEmail() != null
                        && user.getEmail().equals(email)
                        && (userId == null || !user.getId().equals(userId)));
        if (exists) {
            throw new ConflictException("Email already exists");
        }
    }
}