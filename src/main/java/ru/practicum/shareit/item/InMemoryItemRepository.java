package ru.practicum.shareit.item;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class InMemoryItemRepository implements ItemRepository {
    private final Map<Long, Item> items = new HashMap<>();
    private long nextId = 1;

    @Override
    public Item save(Item item) {
        if (item.getId() == null) {
            item.setId(nextId++);
        }
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item findById(long itemId) {
        Item item = items.get(itemId);
        if (item == null) {
            throw new IllegalArgumentException("Item not found");
        }
        return item;
    }

    @Override
    public List<Item> findByOwnerId(long ownerId) {
        List<Item> result = new ArrayList<>();
        for (Item item : items.values()) {
            if (item.getOwner() != null
                    && item.getOwner().getId() != null
                    && item.getOwner().getId() == ownerId) {
                result.add(item);
            }
        }
        return result;
    }

    @Override
    public List<Item> searchAvailable(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        String q = text.toLowerCase();
        List<Item> result = new ArrayList<>();
        for (Item item : items.values()) {
            if (Boolean.TRUE.equals(item.getAvailable())) {
                String name = item.getName() == null ? "" : item.getName().toLowerCase();
                String description = item.getDescription() == null ? "" : item.getDescription().toLowerCase();
                if (name.contains(q) || description.contains(q)) {
                    result.add(item);
                }
            }
        }
        return result;
    }
}