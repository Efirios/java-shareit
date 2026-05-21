package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.practicum.shareit.validation.Create;

import java.util.List;

@Data
public class ItemDto {
    private Long id;

    @NotBlank(groups = Create.class)
    @Size(max = 255, groups = Create.class)
    private String name;

    @NotBlank(groups = Create.class)
    @Size(max = 2000, groups = Create.class)
    private String description;

    @NotNull(groups = Create.class)
    private Boolean available;

    private Long requestId;

    private BookingShortDto lastBooking;
    private BookingShortDto nextBooking;
    private List<CommentDto> comments;
}