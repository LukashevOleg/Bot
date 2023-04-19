package ru.vsu.cs.Lukashev.model.repository;

import org.springframework.data.repository.CrudRepository;
import ru.vsu.cs.Lukashev.model.entity.Event;
import ru.vsu.cs.Lukashev.model.entity.User;

import java.util.List;

public interface EventRepository extends CrudRepository<Event, Long> {

    List<Event> findByOwnerID(User user);

}
