package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
    public ItemDto create(long userId, ItemDto dto) {
        User owner = getUser(userId);
        Item item = ItemMapper.toItem(dto, owner);
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    @Transactional
    public ItemDto update(long userId, long itemId, UpdateItemDto dto) {
        checkUser(userId);
        Item item = getItem(itemId);
        if (!item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Вещь с id=" + itemId + " не принадлежит пользователю");
        }
        if (dto.getName() != null) {
            if (dto.getName().isBlank()) {
                throw new ValidationException("Название вещи не должно быть пустым");
            }
            item.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            if (dto.getDescription().isBlank()) {
                throw new ValidationException("Описание вещи не должно быть пустым");
            }
            item.setDescription(dto.getDescription());
        }
        if (dto.getAvailable() != null) {
            item.setAvailable(dto.getAvailable());
        }
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public ItemDto getById(long itemId) {
        Item item = getItem(itemId);
        ItemDto dto = ItemMapper.toItemDto(item);
        dto.setComments(commentRepository.findByItemId(itemId).stream()
                .map(CommentMapper::toCommentDto).toList());
        return dto;
    }

    @Override
    public List<ItemDto> getByOwner(long userId) {
        checkUser(userId);
        List<Item> items = itemRepository.findByOwnerId(userId);

        if (items.isEmpty()) {
            return Collections.emptyList();
        }

        LocalDateTime now = LocalDateTime.now();

        Map<Long, List<Booking>> bookingsByItemId = bookingRepository.findAllByItemIn(items).stream()
                .collect(Collectors.groupingBy(b -> b.getItem().getId()));

        Map<Long, List<Comment>> commentsByItemId = commentRepository.findAllByItemIn(items).stream()
                .collect(Collectors.groupingBy(c -> c.getItem().getId()));

        return items.stream().map(item -> {
            ItemDto dto = ItemMapper.toItemDto(item);

            List<Booking> bookings = bookingsByItemId.getOrDefault(item.getId(), Collections.emptyList());

            bookings.stream()
                    .filter(b -> b.getStart().isBefore(now))
                    .max(Comparator.comparing(Booking::getStart))
                    .ifPresent(b -> dto.setLastBooking(b.getStart()));

            bookings.stream()
                    .filter(b -> b.getStart().isAfter(now))
                    .min(Comparator.comparing(Booking::getStart))
                    .ifPresent(b -> dto.setNextBooking(b.getStart()));

            dto.setComments(commentsByItemId.getOrDefault(item.getId(), Collections.emptyList()).stream()
                    .map(CommentMapper::toCommentDto)
                    .toList());

            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        return itemRepository.searchAvailable(text).stream().map(ItemMapper::toItemDto).toList();
    }

    @Override
    @Transactional
    public CommentDto addComment(long userId, long itemId, CommentDto dto) {
        User author = getUser(userId);
        Item item = getItem(itemId);

        List<Booking> completedBookings = bookingRepository
                .findCompletedBookingsForUserAndItem(itemId, userId, LocalDateTime.now());
        if (completedBookings.isEmpty()) {
            throw new ValidationException("Пользователь не брал эту вещь в аренду или срок аренды не завершён");
        }

        Comment comment = CommentMapper.toComment(dto, item, author);
        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    private Item getItem(long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + id + " не найдена"));
    }

    private User getUser(long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден"));
    }

    private void checkUser(long id) {
        getUser(id);
    }
}