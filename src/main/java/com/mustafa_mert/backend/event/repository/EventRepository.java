package com.mustafa_mert.backend.event.repository;

import com.mustafa_mert.backend.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByNameContainingIgnoreCaseAndCategoryIgnoreCaseOrderByDateTimeAsc(String name, String category);

    List<Event> findByNameContainingIgnoreCaseOrderByDateTimeAsc(String name);

    List<Event> findByCategoryIgnoreCaseOrderByDateTimeAsc(String category);

    List<Event> findAllByOrderByDateTimeAsc();
}