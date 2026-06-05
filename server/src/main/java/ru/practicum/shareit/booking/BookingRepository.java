package ru.practicum.shareit.booking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Page<Booking> findAllByBooker_Id(Long bookerId, Pageable page);

    Page<Booking> findAllByBooker_IdAndStartIsAfter(Long bookerId, LocalDateTime start, Pageable page);

    Page<Booking> findAllByBooker_IdAndEndIsBefore(Long bookerId, LocalDateTime end, Pageable page);

    Page<Booking> findAllByBooker_IdAndStartIsBeforeAndEndIsAfter(Long bookerId, LocalDateTime now1, LocalDateTime now2, Pageable page);

    Page<Booking> findAllByBooker_IdAndStatus(Long bookerId, BookingStatus status, Pageable page);

    Page<Booking> findAllByItemOwner_Id(Long ownerId, Pageable page);

    Page<Booking> findAllByItemOwner_IdAndStartIsAfter(Long ownerId, LocalDateTime start, Pageable page);

    Page<Booking> findAllByItemOwner_IdAndEndIsBefore(Long ownerId, LocalDateTime end, Pageable page);

    Page<Booking> findAllByItemOwner_IdAndStartIsBeforeAndEndIsAfter(Long ownerId, LocalDateTime now1, LocalDateTime now2, Pageable page);

    Page<Booking> findAllByItemOwner_IdAndStatus(Long ownerId, BookingStatus status, Pageable page);

    Optional<Booking> findFirstByItem_IdAndStatusAndStartIsBeforeOrderByEndDesc(Long itemId, BookingStatus status, LocalDateTime now);

    Optional<Booking> findFirstByItem_IdAndStatusAndStartIsAfterOrderByStartAsc(Long itemId, BookingStatus status, LocalDateTime now);

    boolean existsByItem_IdAndBooker_IdAndStatusAndEndLessThanEqual(Long itemId, Long bookerId,
                                                                    BookingStatus status, LocalDateTime end);

    @Query("select b " +
            "from Booking b " +
            "join fetch b.item i " +
            "join fetch b.booker " +
            "where b.id = ?1 and i.owner.id = ?2")
    Optional<Booking> findByIdAndItem_Owner_Id(Long bookingId, Long ownerId);

    @Query("select b " +
            "from Booking b " +
            "join fetch b.item i " +
            "join fetch b.booker " +
            "where i in ?1 and b.status = ?2")
    List<Booking> findByItemInAndStatusWithBooker(List<Item> items, BookingStatus status, Sort sort);
}