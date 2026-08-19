package ru.practicum.shareit.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.practicum.shareit.validation.Create;
import ru.practicum.shareit.validation.Update;

@Data
public class UserDto {
    private Long id;

    @NotBlank(groups = Create.class)
    @Size(max = 255, groups = Create.class)
    private String name;

    @NotBlank(groups = Create.class)
    @Email(groups = {Create.class, Update.class})
    @Size(max = 512, groups = {Create.class, Update.class})
    private String email;
}