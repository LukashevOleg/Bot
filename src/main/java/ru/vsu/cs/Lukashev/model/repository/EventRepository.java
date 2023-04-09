package ru.vsu.cs.Lukashev.model.repository;

import org.springframework.data.repository.CrudRepository;
import ru.vsu.cs.Lukashev.model.entity.Event;

public interface EventRepository extends CrudRepository<Event, Long> {
}
