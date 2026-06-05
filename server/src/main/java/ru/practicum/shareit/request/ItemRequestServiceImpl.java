package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestItemDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public ItemRequestDto create(long userId, ItemRequestDto dto) {
        User requestor = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        ItemRequest saved = requestRepository.save(ItemRequestMapper.toItemRequest(dto, requestor));
        return ItemRequestMapper.toDto(saved, List.of());
    }

    @Override
    public List<ItemRequestDto> getOwn(long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        List<ItemRequest> requests = requestRepository.findAllByRequestor_IdOrderByCreatedDesc(userId);
        Map<Long, List<ItemRequestItemDto>> itemsByRequestId = getItemsByRequests(requests);

        return requests.stream()
                .map(r -> ItemRequestMapper.toDto(r, itemsByRequestId.getOrDefault(r.getId(), List.of())))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getAll(long userId, int from, int size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Sort sort = Sort.by(Sort.Direction.DESC, "created");
        Pageable page = PageRequest.of(from / size, size, sort);

        List<ItemRequest> requests = requestRepository.findAllByRequestor_IdNot(userId, page).getContent();
        Map<Long, List<ItemRequestItemDto>> itemsByRequestId = getItemsByRequests(requests);

        return requests.stream()
                .map(r -> ItemRequestMapper.toDto(r, itemsByRequestId.getOrDefault(r.getId(), List.of())))
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestDto getById(long userId, long requestId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        ItemRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request not found"));

        List<ItemRequestItemDto> items = itemRepository.findAllByRequest_Id(requestId).stream()
                .map(ItemRequestMapper::toItemDto)
                .collect(Collectors.toList());

        return ItemRequestMapper.toDto(request, items);
    }

    private Map<Long, List<ItemRequestItemDto>> getItemsByRequests(List<ItemRequest> requests) {
        List<Long> ids = requests.stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList());

        if (ids.isEmpty()) {
            return Map.of();
        }

        List<Item> items = itemRepository.findAllByRequest_IdIn(ids);

        return items.stream()
                .collect(Collectors.groupingBy(
                        i -> i.getRequest().getId(),
                        Collectors.mapping(ItemRequestMapper::toItemDto, Collectors.toList())
                ));
    }
}