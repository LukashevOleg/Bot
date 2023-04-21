package ru.vsu.cs.Lukashev.repository;

import org.springframework.data.repository.CrudRepository;
import ru.vsu.cs.Lukashev.entity.User;

public interface UserRepository extends CrudRepository<User, Long> {

}
