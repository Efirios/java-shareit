package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
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
        Item item = ItemMapper.toItem(itemDto, owner, null);
        return ItemMapper.toDto(itemRepository.save(item));
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

        LocalDateTime now = LocalDateTime.now();
        return itemRepository.findAllByOwner_Id(userId, page)
                .getContent()
                .stream()
                .map(item -> {
                    Booking last = bookingRepository
                            .findFirstByItem_IdAndStatusAndStartIsBeforeOrderByEndDesc(item.getId(), BookingStatus.APPROVED, now)
                            .orElse(null);
                    Booking next = bookingRepository
                            .findFirstByItem_IdAndStatusAndStartIsAfterOrderByStartAsc(item.getId(), BookingStatus.APPROVED, now)
                            .orElse(null);
                    List<CommentDto> comments = commentRepository.findAllByItem_Id(item.getId()).stream()
                            .map(CommentMapper::toDto)
                            .collect(Collectors.toList());
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
        if (commentDto.getText() == null || commentDto.getText().isBlank()) {
            throw new BadRequestException("Text is blank");
        }

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

        Comment comment = new Comment();
        comment.setText(commentDto.getText());
        comment.setItem(item);
        comment.setAuthor(author);

        return CommentMapper.toDto(commentRepository.save(comment));
    }
}