package ru.vsu.cs.Lukashev.entity;

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

    @ManyToOne
    @JoinColumn(name = "owner_id", referencedColumnName = "id")
    private User ownerID;

    @ManyToOne
    @JoinColumn(name = "subscriber_id", referencedColumnName = "id")
    private User subscriberID;

    @ManyToOne
    @JoinColumn(name = "folder_id", referencedColumnName = "id")
    private Folder folderID;


//    @Column(name = "is_folder")
//    private boolean isFolder;

}
