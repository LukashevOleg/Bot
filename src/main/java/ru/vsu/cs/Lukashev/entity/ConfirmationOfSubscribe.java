package ru.vsu.cs.Lukashev.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "confirmation_of_subscribe")
@Getter
@Setter
public class ConfirmationOfSubscribe {
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

    @Column(name = "is_confirm")
    private boolean isConfirm;
}
