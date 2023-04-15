package ru.vsu.cs.Lukashev.calendarData;

public enum DayOfWeekEnum {
    MONDAY(     "пн",   1),
    TUESDAY(    "вт",   2),
    WEDNESDAY(  "ср",   3),
    THURSDAY(   "чт",   4),
    FRIDAY(     "пт",   5),
    SATURDAY(   "сб",   6),
    SUNDAY(     "вскр", 7);

    private String name;
    private int number;

    DayOfWeekEnum(String name, int number) {
        this.name   = name;
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public int getNumber() {
        return number;
    }
}
