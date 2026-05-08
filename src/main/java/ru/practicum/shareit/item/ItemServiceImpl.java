package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public ItemDto create(long userId, ItemDto itemDto) {
        User owner = userRepository.findById(userId);
        Item item = ItemMapper.toItem(itemDto, owner, null);
        return ItemMapper.toDto(itemRepository.save(item));
    }

    @Override
    public ItemDto update(long userId, long itemId, ItemDto itemDto) {
        userRepository.findById(userId);
        Item existing = itemRepository.findById(itemId);

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
        userRepository.findById(userId);
        return ItemMapper.toDto(itemRepository.findById(itemId));
    }

    @Override
    public List<ItemDto> getByOwner(long userId) {
        userRepository.findById(userId);
        return itemRepository.findByOwnerId(userId).stream()
                .map(ItemMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(long userId, String text) {
        userRepository.findById(userId);
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return itemRepository.searchAvailable(text).stream()
                .map(ItemMapper::toDto)
                .collect(Collectors.toList());
    }
}