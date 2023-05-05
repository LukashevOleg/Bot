package ru.vsu.cs.Lukashev.repository;

import org.springframework.data.repository.CrudRepository;
import ru.vsu.cs.Lukashev.entity.ConfirmationOfSubscribe;
import ru.vsu.cs.Lukashev.entity.Event;
import ru.vsu.cs.Lukashev.entity.User;

import java.util.List;

public interface ConfirmationOfSubscribeRepository  extends CrudRepository<ConfirmationOfSubscribe, Long> {

    ConfirmationOfSubscribe findByOwnerIDAndSubscriberIDAndIsConfirm(User ownerID, User subscriberID, boolean isConfirm);
    ConfirmationOfSubscribe findByOwnerIDAndSubscriberID(User ownerID, User subscriberID);
    List<ConfirmationOfSubscribe> findByOwnerIDAndIsConfirm(User user, boolean isConfirm);
}
