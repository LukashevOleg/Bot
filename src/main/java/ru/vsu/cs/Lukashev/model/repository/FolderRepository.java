package ru.vsu.cs.Lukashev.model.repository;

import org.springframework.data.repository.CrudRepository;
import ru.vsu.cs.Lukashev.model.entity.Folder;

public interface FolderRepository extends CrudRepository<Folder, Long> {
}
