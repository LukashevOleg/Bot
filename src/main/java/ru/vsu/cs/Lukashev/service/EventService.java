package ru.vsu.cs.Lukashev.service;

import org.jvnet.hk2.annotations.Service;
import org.springframework.beans.factory.annotation.Autowired;
import ru.vsu.cs.Lukashev.repository.EventRepository;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

//    public void addEvent(Date date, ){
//
//    }




}
