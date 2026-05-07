package ru.practicum.shareit.item;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    public ItemServiceImpl(ItemRepository itemRepository, UserRepository userRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    @Override
    public ItemDto create(long userId, ItemDto itemDto) {
        User owner = userRepository.findById(userId);
        Item item = ItemMapper.toItem(itemDto, owner, null);
        return ItemMapper.toDto(itemRepository.save(item));
    }

    @Override
    public ItemDto update(long userId, long itemId, ItemDto itemDto) {
        Item existing = itemRepository.findById(itemId);
        if (existing.getOwner() == null
                || existing.getOwner().getId() == null
                || existing.getOwner().getId() != userId) {
            throw new IllegalArgumentException("Only owner can update item");
        }
        if (itemDto.getName() != null) {
            existing.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            existing.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existing.setAvailable(itemDto.getAvailable());
        }
        return ItemMapper.toDto(itemRepository.save(existing));
    }

    @Override
    public ItemDto getById(long userId, long itemId) {
        return ItemMapper.toDto(itemRepository.findById(itemId));
    }

    @Override
    public List<ItemDto> getByOwner(long userId) {
        List<Item> items = itemRepository.findByOwnerId(userId);
        List<ItemDto> result = new ArrayList<>();
        for (Item item : items) {
            result.add(ItemMapper.toDto(item));
        }
        return result;
    }

    @Override
    public List<ItemDto> search(long userId, String text) {
        List<Item> items = itemRepository.searchAvailable(text);
        List<ItemDto> result = new ArrayList<>();
        for (Item item : items) {
            result.add(ItemMapper.toDto(item));
        }
        return result;
    }
}