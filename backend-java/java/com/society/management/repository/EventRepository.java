package com.society.management.repository;

import com.society.management.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.OffsetDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByEventDateAfterOrderByEventDateAsc(OffsetDateTime now);
}
