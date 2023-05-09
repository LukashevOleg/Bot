package ru.vsu.cs.Lukashev.service;

import ru.vsu.cs.Lukashev.entity.Folder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Pagination {

    public static <T> List<T> getEntityListForPage(List<T> entityList, int pageNumber, int countElOnPage){
        int curIndex = 0;

        Iterator<T> linkListIterator = entityList.iterator();
        while (linkListIterator.hasNext() && curIndex < (pageNumber * countElOnPage)){
            linkListIterator.next();
            curIndex++;
        }

        // задаю количество на странице
        int uppLimitLinkUserIndex = curIndex + countElOnPage;
        List<T> returnListEntity = new ArrayList<>();
        while (linkListIterator.hasNext() && curIndex < uppLimitLinkUserIndex) {
            returnListEntity.add(linkListIterator.next());
            curIndex++;
        }
        return returnListEntity;
    }
}
