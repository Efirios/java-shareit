package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class BookingCreateDtoJsonTest {

    @Autowired
    private JacksonTester<BookingCreateDto> json;

    @Test
    void serialize() throws Exception {
        BookingCreateDto dto = new BookingCreateDto();
        dto.setItemId(1L);
        dto.setStart(LocalDateTime.of(2026, 6, 4, 12, 0));
        dto.setEnd(LocalDateTime.of(2026, 6, 4, 13, 0));

        var content = json.write(dto);

        assertThat(content).extractingJsonPathNumberValue("$.itemId").isEqualTo(1);
        assertThat(content).extractingJsonPathStringValue("$.start").isEqualTo("2026-06-04T12:00:00");
        assertThat(content).extractingJsonPathStringValue("$.end").isEqualTo("2026-06-04T13:00:00");
    }
}