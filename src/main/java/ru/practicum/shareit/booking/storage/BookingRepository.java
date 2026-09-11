package ru.practicum.shareit.booking.storage;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByBookerId(Long bookerId, Sort sort);

    List<Booking> findByBookerIdAndStartBeforeAndEndAfter(Long bookerId, LocalDateTime start, LocalDateTime end, Sort sort);

    List<Booking> findByBookerIdAndEndBefore(Long bookerId, LocalDateTime end, Sort sort);

    List<Booking> findByBookerIdAndStartAfter(Long bookerId, LocalDateTime start, Sort sort);

    List<Booking> findByBookerIdAndStatus(Long bookerId, BookingStatus status, Sort sort);

    List<Booking> findByItemOwnerId(Long ownerId, Sort sort);

    List<Booking> findByItemOwnerIdAndStartBeforeAndEndAfter(Long ownerId, LocalDateTime start, LocalDateTime end, Sort sort);

    List<Booking> findByItemOwnerIdAndEndBefore(Long ownerId, LocalDateTime end, Sort sort);

    List<Booking> findByItemOwnerIdAndStartAfter(Long ownerId, LocalDateTime start, Sort sort);

    List<Booking> findByItemOwnerIdAndStatus(Long ownerId, BookingStatus status, Sort sort);

    @Query("select b from Booking b " +
            "where b.item.id = ?1 and b.status = 'APPROVED' " +
            "and b.start < ?2 " +
            "order by b.end desc")
    List<Booking> findLastBooking(Long itemId, LocalDateTime now);

    @Query("select b from Booking b " +
            "where b.item.id = ?1 and b.status = 'APPROVED' " +
            "and b.start > ?2 " +
            "order by b.start asc")
    List<Booking> findNextBooking(Long itemId, LocalDateTime now);

    @Query("select b from Booking b " +
            "where b.item.id = ?1 and b.booker.id = ?2 " +
            "and b.status = 'APPROVED' " +
            "and b.end < ?3")
    List<Booking> findCompletedBookingsForUserAndItem(Long itemId, Long userId, LocalDateTime now);
}