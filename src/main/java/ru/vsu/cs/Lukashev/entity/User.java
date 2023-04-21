package ru.vsu.cs.Lukashev.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Entity
@Table(name = "\"User\"")
@Getter @Setter
public class User {

    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "\"name\"")
    private String name;

//    @OneToMany
//    @JoinColumn(name = "owner_id")
//    List<Event> events = new ArrayList<>();
//
//    @OneToMany
//    @JoinColumn(name = "folder_id")
//    List<Event> folders = new ArrayList<>();

//    @OneToMany
//    @JoinColumn(name = "user_id")
//    List<Event> permissionForMyEvents = new ArrayList<>();
//
//    @OneToMany
//    @JoinColumn(name = "child_id")
//    List<Event> permissionForAnotherEvents = new ArrayList<>();

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id && Objects.equals(name, user.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
