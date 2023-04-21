package ru.vsu.cs.Lukashev.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;

@Entity
@Table(name = "Event")
@Getter
@Setter
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;


    @ManyToOne
    @JoinColumn(name = "owner_id", referencedColumnName = "id")
    private User ownerID;


    @ManyToOne
    @JoinColumn(name = "folder_id", referencedColumnName = "id")
    private Folder folderID;

    @Column(name = "date")
    private Date date;

}
