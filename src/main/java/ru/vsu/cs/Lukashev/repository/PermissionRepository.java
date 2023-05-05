package ru.vsu.cs.Lukashev.repository;

import org.springframework.data.repository.CrudRepository;
import ru.vsu.cs.Lukashev.entity.Permission;
import ru.vsu.cs.Lukashev.entity.User;

import java.util.List;

public interface PermissionRepository extends CrudRepository<Permission, Long> {

    List<Permission> findByOwnerID(User user);
    List<Permission> findBySubscriberIDAndOwnerID(User subscriberID, User ownerID);


}
