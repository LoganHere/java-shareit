package ru.practicum.shareit.item.storage;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByItemId(Long itemId);

    @EntityGraph(attributePaths = {"author"})
    @Query("select c from Comment c " +
            "where c.item in ?1 " +
            "order by c.created asc")
    List<Comment> findAllByItemIn(List<Item> items);
}