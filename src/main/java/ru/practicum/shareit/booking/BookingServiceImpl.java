package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BookingDto add(long userId, BookingCreateDto bookingCreateDto) {
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Item item = itemRepository.findById(bookingCreateDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Item not found"));

        if (!Boolean.TRUE.equals(item.getAvailable())) {
            throw new BadRequestException("Item is not available");
        }

        if (item.getOwner() != null && item.getOwner().getId() != null && item.getOwner().getId() == userId) {
            throw new BadRequestException("Owner can't book own item");
        }

        if (bookingCreateDto.getStart() == null || bookingCreateDto.getEnd() == null) {
            throw new BadRequestException("Start or end is null");
        }

        if (!bookingCreateDto.getEnd().isAfter(bookingCreateDto.getStart())) {
            throw new BadRequestException("End must be after start");
        }

        if (!bookingCreateDto.getStart().isAfter(LocalDateTime.now())) {
            throw new BadRequestException("Start must be in future");
        }

        Booking booking = new Booking();
        booking.setStart(bookingCreateDto.getStart());
        booking.setEnd(bookingCreateDto.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);

        return BookingMapper.toDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingDto approve(long userId, long bookingId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        if (booking.getItem() == null
                || booking.getItem().getOwner() == null
                || booking.getItem().getOwner().getId() == null
                || booking.getItem().getOwner().getId() != userId) {
            throw new ForbiddenException("Only owner can approve booking");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new BadRequestException("Booking status is not waiting");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return BookingMapper.toDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto getById(long userId, long bookingId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        long ownerId = booking.getItem().getOwner().getId();
        long bookerId = booking.getBooker().getId();
        if (ownerId != userId && bookerId != userId) {
            throw new ForbiddenException("Access denied");
        }

        return BookingMapper.toDto(booking);
    }

    @Override
    public List<BookingDto> getByBooker(long userId, String state, int from, int size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        BookingState bookingState = parseState(state);

        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        Pageable page = PageRequest.of(from / size, size, sort);
        LocalDateTime now = LocalDateTime.now();

        return findByBookerState(userId, bookingState, page, now).stream()
                .map(BookingMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getByOwner(long userId, String state, int from, int size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        BookingState bookingState = parseState(state);

        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        Pageable page = PageRequest.of(from / size, size, sort);
        LocalDateTime now = LocalDateTime.now();

        return findByOwnerState(userId, bookingState, page, now).stream()
                .map(BookingMapper::toDto)
                .collect(Collectors.toList());
    }

    private List<Booking> findByBookerState(long userId, BookingState state, Pageable page, LocalDateTime now) {
        return switch (state) {
            case ALL -> bookingRepository.findAllByBooker_Id(userId, page).getContent();
            case CURRENT -> bookingRepository.findAllByBooker_IdAndStartIsBeforeAndEndIsAfter(userId, now, now, page).getContent();
            case PAST -> bookingRepository.findAllByBooker_IdAndEndIsBefore(userId, now, page).getContent();
            case FUTURE -> bookingRepository.findAllByBooker_IdAndStartIsAfter(userId, now, page).getContent();
            case WAITING -> bookingRepository.findAllByBooker_IdAndStatus(userId, BookingStatus.WAITING, page).getContent();
            case REJECTED -> bookingRepository.findAllByBooker_IdAndStatus(userId, BookingStatus.REJECTED, page).getContent();
        };
    }

    private List<Booking> findByOwnerState(long userId, BookingState state, Pageable page, LocalDateTime now) {
        return switch (state) {
            case ALL -> bookingRepository.findAllByItemOwner_Id(userId, page).getContent();
            case CURRENT -> bookingRepository.findAllByItemOwner_IdAndStartIsBeforeAndEndIsAfter(userId, now, now, page).getContent();
            case PAST -> bookingRepository.findAllByItemOwner_IdAndEndIsBefore(userId, now, page).getContent();
            case FUTURE -> bookingRepository.findAllByItemOwner_IdAndStartIsAfter(userId, now, page).getContent();
            case WAITING -> bookingRepository.findAllByItemOwner_IdAndStatus(userId, BookingStatus.WAITING, page).getContent();
            case REJECTED -> bookingRepository.findAllByItemOwner_IdAndStatus(userId, BookingStatus.REJECTED, page).getContent();
        };
    }

    private BookingState parseState(String state) {
        if (state == null) {
            return BookingState.ALL;
        }
        try {
            return BookingState.valueOf(state);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Unknown state: " + state);
        }
    }
}