package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.validation.Create;

import java.util.List;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService service;

    @PostMapping
    public UserDto create(@Validated(Create.class) @RequestBody UserDto userDto) {
        return service.create(userDto);
    }

    @PatchMapping("/{userId}")
    public UserDto update(@PathVariable long userId,
                          @RequestBody UserDto userDto) {
        return service.update(userId, userDto);
    }

    @GetMapping("/{userId}")
    public UserDto getById(@PathVariable long userId) {
        return service.getById(userId);
    }

    @GetMapping
    public List<UserDto> getAll() {
        return service.getAll();
    }

    @DeleteMapping("/{userId}")
    public void delete(@PathVariable long userId) {
        service.delete(userId);
    }
}