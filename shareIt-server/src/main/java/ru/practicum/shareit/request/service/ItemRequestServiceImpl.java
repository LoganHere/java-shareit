package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ItemRequestResponseDto create(long userId, ItemRequestDto dto) {
        User requestor = getUser(userId);
        ItemRequest request = ItemRequestMapper.toItemRequest(dto.getDescription(), requestor);
        ItemRequest saved = itemRequestRepository.save(request);
        return ItemRequestMapper.toItemRequestResponseDto(saved, Collections.emptyList());
    }

    @Override
    public List<ItemRequestResponseDto> getAllByRequestor(long userId) {
        checkUser(userId);
        List<ItemRequest> requests = itemRequestRepository.findByRequestorIdOrderByCreatedDesc(userId);
        return buildResponses(requests);
    }

    @Override
    public List<ItemRequestResponseDto> getAllByOtherUsers(long userId) {
        checkUser(userId);
        List<ItemRequest> requests = itemRequestRepository.findByRequestorIdNotOrderByCreatedDesc(userId);
        return buildResponses(requests);
    }

    @Override
    public ItemRequestResponseDto getById(long userId, long requestId) {
        checkUser(userId);
        ItemRequest request = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с id=" + requestId + " не найден"));
        List<Item> items = itemRepository.findByRequestIdIn(List.of(requestId));
        return ItemRequestMapper.toItemRequestResponseDto(request, items);
    }

    private List<ItemRequestResponseDto> buildResponses(List<ItemRequest> requests) {
        if (requests.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> requestIds = requests.stream().map(ItemRequest::getId).toList();
        Map<Long, List<Item>> itemsByRequestId = itemRepository.findByRequestIdIn(requestIds).stream()
                .collect(Collectors.groupingBy(item -> item.getRequest().getId()));
        return requests.stream()
                .map(request -> ItemRequestMapper.toItemRequestResponseDto(
                        request,
                        itemsByRequestId.getOrDefault(request.getId(), Collections.emptyList())))
                .toList();
    }

    private User getUser(long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден"));
    }

    private void checkUser(long id) {
        getUser(id);
    }
}