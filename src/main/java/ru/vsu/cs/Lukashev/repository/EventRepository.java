package ru.vsu.cs.Lukashev.repository;

import org.springframework.data.repository.CrudRepository;
import ru.vsu.cs.Lukashev.entity.Event;
import ru.vsu.cs.Lukashev.entity.Folder;
import ru.vsu.cs.Lukashev.entity.User;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends CrudRepository<Event, Long> {

    List<Event> findByOwnerID(User user);

    List<Event> findByFolderID(Folder folder);

    List<Event> findByDate(Date date);


}
