package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    @Override
    @Transactional
    public UserDto create(UserDto userDto) {
        if (userDto.getEmail() != null) {
            checkEmailUnique(null, userDto.getEmail());
        }
        User user = UserMapper.toUser(userDto);
        return UserMapper.toDto(repository.save(user));
    }

    @Override
    @Transactional
    public UserDto update(long userId, UserDto userDto) {
        User existing = repository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

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
        return UserMapper.toDto(repository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found")));
    }

    @Override
    public List<UserDto> getAll() {
        return repository.findAll().stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(long userId) {
        repository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        repository.deleteById(userId);
    }

    private void checkEmailUnique(Long userId, String email) {
        User found = repository.findByEmail(email).orElse(null);
        if (found != null && (userId == null || !found.getId().equals(userId))) {
            throw new ConflictException("Email already exists");
        }
    }
}