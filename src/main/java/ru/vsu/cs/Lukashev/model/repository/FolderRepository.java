package ru.vsu.cs.Lukashev.model.repository;

import org.springframework.data.repository.CrudRepository;
import ru.vsu.cs.Lukashev.model.entity.Event;
import ru.vsu.cs.Lukashev.model.entity.Folder;
import ru.vsu.cs.Lukashev.model.entity.User;

import java.util.List;

public interface FolderRepository extends CrudRepository<Folder, Long> {

    List<Folder> findByOwnerID(User user);

}
