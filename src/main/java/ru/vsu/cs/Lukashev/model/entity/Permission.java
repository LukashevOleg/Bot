package ru.vsu.cs.Lukashev.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;

@Entity
@Table(name = "Permission")
@Getter
@Setter
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "user_id")
    private long userID;

    @Column(name = "child_id")
    private long childID;

    @Column(name = "date")
    private Date date;


    @Column(name = "subscribed_id")
    private long subscribedID;

    @Column(name = "is_folder")
    private boolean isFolder;

}
