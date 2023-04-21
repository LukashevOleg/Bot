package ru.vsu.cs.Lukashev.repository;

import org.springframework.data.repository.CrudRepository;
import ru.vsu.cs.Lukashev.entity.Permission;

public interface PermissionRepository extends CrudRepository<Permission, Long> {

}
