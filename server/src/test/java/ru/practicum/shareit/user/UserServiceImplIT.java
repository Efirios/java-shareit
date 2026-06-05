package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class UserServiceImplIT {

    @Autowired
    private UserService userService;

    @Test
    void create_shouldSaveUser() {
        UserDto dto = new UserDto();
        dto.setName("user");
        dto.setEmail("user@mail.ru");

        UserDto created = userService.create(dto);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("user");
        assertThat(created.getEmail()).isEqualTo("user@mail.ru");
    }
}