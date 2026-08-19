package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ItemServiceImplIT {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Test
    void getByOwner_shouldReturnItemsWithBookingsAndComments() {
        User owner = new User();
        owner.setName("owner");
        owner.setEmail("owner@mail.ru");
        owner = userRepository.save(owner);

        User booker = new User();
        booker.setName("booker");
        booker.setEmail("booker@mail.ru");
        booker = userRepository.save(booker);

        Item item = new Item();
        item.setName("item");
        item.setDescription("desc");
        item.setAvailable(true);
        item.setOwner(owner);
        item = itemRepository.save(item);

        LocalDateTime now = LocalDateTime.now();

        Booking lastBooking = new Booking();
        lastBooking.setItem(item);
        lastBooking.setBooker(booker);
        lastBooking.setStatus(BookingStatus.APPROVED);
        lastBooking.setStart(now.minusDays(2));
        lastBooking.setEnd(now.minusDays(1));
        lastBooking = bookingRepository.save(lastBooking);

        Booking nextBooking = new Booking();
        nextBooking.setItem(item);
        nextBooking.setBooker(booker);
        nextBooking.setStatus(BookingStatus.APPROVED);
        nextBooking.setStart(now.plusDays(1));
        nextBooking.setEnd(now.plusDays(2));
        nextBooking = bookingRepository.save(nextBooking);

        Comment comment = new Comment();
        comment.setText("text");
        comment.setItem(item);
        comment.setAuthor(booker);
        commentRepository.save(comment);

        List<ItemDto> result = itemService.getByOwner(owner.getId(), 0, 10);

        assertThat(result).hasSize(1);
        ItemDto dto = result.get(0);

        assertThat(dto.getId()).isEqualTo(item.getId());
        assertThat(dto.getLastBooking()).isNotNull();
        assertThat(dto.getLastBooking().getId()).isEqualTo(lastBooking.getId());
        assertThat(dto.getNextBooking()).isNotNull();
        assertThat(dto.getNextBooking().getId()).isEqualTo(nextBooking.getId());
        assertThat(dto.getComments()).hasSize(1);
        assertThat(dto.getComments().get(0).getText()).isEqualTo("text");
    }
}