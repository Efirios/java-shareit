package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ItemRequestServiceImplIT {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void getOwn_shouldReturnRequestsWithItems() {
        User requestor = new User();
        requestor.setName("requestor");
        requestor.setEmail("requestor@mail.ru");
        requestor = userRepository.save(requestor);

        User owner = new User();
        owner.setName("owner");
        owner.setEmail("owner@mail.ru");
        owner = userRepository.save(owner);

        ItemRequestDto createDto = new ItemRequestDto();
        createDto.setDescription("need item");

        ItemRequestDto created = itemRequestService.create(requestor.getId(), createDto);

        ItemRequest request = itemRequestRepository.findById(created.getId()).orElseThrow();

        Item item = new Item();
        item.setName("item");
        item.setDescription("desc");
        item.setAvailable(true);
        item.setOwner(owner);
        item.setRequest(request);
        itemRepository.save(item);

        List<ItemRequestDto> own = itemRequestService.getOwn(requestor.getId());

        assertThat(own).hasSize(1);
        assertThat(own.get(0).getId()).isEqualTo(created.getId());
        assertThat(own.get(0).getItems()).hasSize(1);
        assertThat(own.get(0).getItems().get(0).getName()).isEqualTo("item");
        assertThat(own.get(0).getItems().get(0).getOwnerId()).isEqualTo(owner.getId());
    }
}