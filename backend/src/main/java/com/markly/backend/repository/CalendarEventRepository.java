package com.markly.backend.repository;

import com.markly.backend.domain.CalendarEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {

    @Query("select e from CalendarEvent e left join fetch e.createdBy order by e.startDate asc")
    List<CalendarEvent> findAllByOrderByStartDateAsc();
}
