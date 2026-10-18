package com.gavioesfc.sorteio.service;

import java.time.LocalDate;
import java.time.MonthDay;

/** Calendar rules for birthdays (only day/month matter; the year is ignored). */
public final class BirthdayCalendar {

    private static final MonthDay LEAP_DAY = MonthDay.of(2, 29);

    private BirthdayCalendar() {
    }

    /**
     * True when someone born on {@code birthDate} celebrates on {@code day}.
     * Leap-day birthdays are celebrated on 28/02 in non-leap years.
     */
    public static boolean isBirthdayOn(LocalDate birthDate, LocalDate day) {
        MonthDay birthday = MonthDay.from(birthDate);
        if (birthday.equals(LEAP_DAY) && !day.isLeapYear()) {
            birthday = MonthDay.of(2, 28);
        }
        return birthday.equals(MonthDay.from(day));
    }
}
