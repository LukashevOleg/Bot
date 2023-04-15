package ru.vsu.cs.Lukashev.model.repository;

import org.springframework.data.repository.CrudRepository;
import ru.vsu.cs.Lukashev.model.entity.Permission;

public interface PermissionRepository extends CrudRepository<Permission, Long> {

}
