package ru.vsu.cs.Lukashev.model;

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


    @ManyToOne
    @JoinColumn(name = "owner_id", referencedColumnName = "id")
    private long ownerID;

    @Column(name = "name")
    private String name;
}
