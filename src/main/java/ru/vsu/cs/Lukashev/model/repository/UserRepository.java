package ru.vsu.cs.Lukashev.model.repository;

import org.springframework.data.repository.CrudRepository;
import ru.vsu.cs.Lukashev.model.entity.User;

public interface UserRepository extends CrudRepository<User, Long> {

}
