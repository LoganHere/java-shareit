package ru.practicum.shareit.item.storage;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryItemStorage implements ItemStorage {
    private final Map<Long, Item> items = new LinkedHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    @Override
    public Item create(Item item) {
        item.setId(sequence.incrementAndGet());
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item update(Item item) {
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Optional<Item> findById(long id) {
        return Optional.ofNullable(items.get(id));
    }

    @Override
    public List<Item> findByOwnerId(long ownerId) {
        return items.values().stream().filter(item -> item.getOwner().getId() == ownerId).toList();
    }

    @Override
    public List<Item> searchAvailable(String text) {
        String query = text.toLowerCase(Locale.ROOT);
        if (query.isBlank()) {
            return new ArrayList<>();
        }
        return items.values().stream()
                .filter(item -> Boolean.TRUE.equals(item.getAvailable()))
                .filter(item -> item.getName().toLowerCase(Locale.ROOT).contains(query)
                        || item.getDescription().toLowerCase(Locale.ROOT).contains(query))
                .toList();
    }
}
