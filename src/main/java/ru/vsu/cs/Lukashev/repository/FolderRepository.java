package ru.vsu.cs.Lukashev.repository;

import org.springframework.data.repository.CrudRepository;
import ru.vsu.cs.Lukashev.entity.Folder;
import ru.vsu.cs.Lukashev.entity.User;

import java.util.List;

public interface FolderRepository extends CrudRepository<Folder, Long> {

    List<Folder> findByOwnerID(User user);

    List<Folder> findAllByIdIn(List<Long> folderId);

}
