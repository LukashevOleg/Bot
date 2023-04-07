package ru.vsu.cs.Lukashev.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "User")
@Getter @Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "name")
    private String name;

    @OneToMany
    @JoinColumn(name = "owner_id")
    List<Event> events = new ArrayList<>();

    @OneToMany
    @JoinColumn(name = "folder_id")
    List<Event> folders = new ArrayList<>();

    @OneToMany
    @JoinColumn(name = "user_id")
    List<Event> permissionForMyEvents = new ArrayList<>();

    @OneToMany
    @JoinColumn(name = "child_id")
    List<Event> permissionForAnotherEvents = new ArrayList<>();

}
