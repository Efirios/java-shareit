package ru.practicum.shareit.item;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    Page<Item> findAllByOwner_Id(Long ownerId, Pageable page);

    @Query("select i from Item i " +
            "where i.available = true and (" +
            "upper(i.name) like upper(concat('%', ?1, '%')) " +
            "or upper(i.description) like upper(concat('%', ?1, '%'))" +
            ")")
    List<Item> search(String text);

    @Query("select i from Item i join fetch i.owner where i.request.id in ?1")
    List<Item> findAllByRequest_IdIn(List<Long> requestIds);

    @Query("select i from Item i join fetch i.owner where i.request.id = ?1")
    List<Item> findAllByRequest_Id(Long requestId);
}