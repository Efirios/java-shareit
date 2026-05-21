package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public ItemDto create(long userId, ItemDto itemDto) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return ItemMapper.toDto(itemRepository.save(ItemMapper.toItem(itemDto, owner, null)));
    }

    @Override
    @Transactional
    public ItemDto update(long userId, long itemId, ItemDto itemDto) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Item existing = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found"));

        if (existing.getOwner() == null
                || existing.getOwner().getId() == null
                || existing.getOwner().getId() != userId) {
            throw new ForbiddenException("Only owner can update item");
        }

        if (itemDto.getName() != null) {
            if (itemDto.getName().isBlank()) {
                throw new BadRequestException("Name is blank");
            }
            existing.setName(itemDto.getName());
        }

        if (itemDto.getDescription() != null) {
            if (itemDto.getDescription().isBlank()) {
                throw new BadRequestException("Description is blank");
            }
            existing.setDescription(itemDto.getDescription());
        }

        if (itemDto.getAvailable() != null) {
            existing.setAvailable(itemDto.getAvailable());
        }

        return ItemMapper.toDto(itemRepository.save(existing));
    }

    @Override
    public ItemDto getById(long userId, long itemId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found"));

        List<CommentDto> comments = commentRepository.findAllByItem_Id(itemId).stream()
                .map(CommentMapper::toDto)
                .collect(Collectors.toList());

        if (item.getOwner() != null && item.getOwner().getId() != null && item.getOwner().getId() == userId) {
            LocalDateTime now = LocalDateTime.now();
            Booking last = bookingRepository
                    .findFirstByItem_IdAndStatusAndStartIsBeforeOrderByEndDesc(itemId, BookingStatus.APPROVED, now)
                    .orElse(null);
            Booking next = bookingRepository
                    .findFirstByItem_IdAndStatusAndStartIsAfterOrderByStartAsc(itemId, BookingStatus.APPROVED, now)
                    .orElse(null);
            return ItemMapper.toDto(item, last, next, comments);
        }

        return ItemMapper.toDto(item, null, null, comments);
    }

    @Override
    public List<ItemDto> getByOwner(long userId, int from, int size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Pageable page = PageRequest.of(from / size, size);
        List<Item> items = itemRepository.findAllByOwner_Id(userId, page).getContent();

        Map<Long, List<CommentDto>> commentMap = commentRepository.findAllByItemIn(items).stream()
                .collect(Collectors.groupingBy(
                        c -> c.getItem().getId(),
                        Collectors.mapping(CommentMapper::toDto, Collectors.toList())
                ));

        Sort bookingSort = Sort.by(Sort.Direction.ASC, "start");
        Map<Long, List<Booking>> bookingMap = bookingRepository.findByItemInAndStatusWithBooker(items, BookingStatus.APPROVED, bookingSort)
                .stream()
                .collect(Collectors.groupingBy(b -> b.getItem().getId()));

        LocalDateTime now = LocalDateTime.now();

        return items.stream()
                .map(item -> {
                    List<Booking> itemBookings = bookingMap.getOrDefault(item.getId(), List.of());
                    Booking last = null;
                    Booking next = null;

                    for (Booking booking : itemBookings) {
                        if (booking.getStart().isBefore(now)) {
                            if (last == null || booking.getEnd().isAfter(last.getEnd())) {
                                last = booking;
                            }
                        } else if (booking.getStart().isAfter(now)) {
                            if (next == null || booking.getStart().isBefore(next.getStart())) {
                                next = booking;
                            }
                        }
                    }

                    List<CommentDto> comments = commentMap.getOrDefault(item.getId(), List.of());
                    return ItemMapper.toDto(item, last, next, comments);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(long userId, String text) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return itemRepository.search(text).stream()
                .map(ItemMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addComment(long userId, long itemId, CommentDto commentDto) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found"));

        boolean allowed = bookingRepository.existsByItem_IdAndBooker_IdAndStatusAndEndIsBefore(
                itemId,
                userId,
                BookingStatus.APPROVED,
                LocalDateTime.now()
        );
        if (!allowed) {
            throw new BadRequestException("User has no completed booking");
        }

        return CommentMapper.toDto(commentRepository.save(CommentMapper.toComment(commentDto, item, author)));
    }
}