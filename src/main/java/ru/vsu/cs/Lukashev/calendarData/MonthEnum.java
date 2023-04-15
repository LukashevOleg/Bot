package ru.vsu.cs.Lukashev.calendarData;

public enum MonthEnum {
    JANUARY(    "январь",   1),
    FEBRUARY(   "февраль",  2),
    MARSH(      "март",     3),
    APRIL(      "апрель",   4),
    MAY(        "май",      5),
    JUNE(       "июнь",     6),
    JULY(       "июль",     7),
    AUGUST(     "август",   8),
    SEPTEMBER(  "сентябрь", 9),
    OCTOBER(    "октябрь",  10),
    NOVEMBER(   "ноябрь",   11),
    DECEMBER(   "декабрь",  12);

    private final String name;
    private final int number;

    MonthEnum(String name, int number) {
        this.name = name;
        this.number = number;
    }

    public String getName(){return name;}
    public int getNumber(){return number;}
}
