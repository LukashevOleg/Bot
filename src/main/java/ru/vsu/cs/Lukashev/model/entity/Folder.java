package ru.vsu.cs.Lukashev.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

//import javax.persistence.*;

@Entity
@Table(name = "Folder")
@Getter
@Setter
public class Folder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;


//    @ManyToOne()
//    @JoinColumn(name = "owner_id", referencedColumnName = "User.id")
//    private long ownerID;

    @Column(name = "ownerID")
    private long ownerID;

    @ManyToOne
    @JoinColumn(name = "ownerID", referencedColumnName = "id", insertable = false, updatable = false)
    private User owner;

    @Column(name = "name")
    private String name;
}
