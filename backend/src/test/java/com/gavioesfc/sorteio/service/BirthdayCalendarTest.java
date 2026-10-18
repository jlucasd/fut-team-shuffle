package com.gavioesfc.sorteio.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class BirthdayCalendarTest {

    @Test
    void matchesSameDayAndMonthIgnoringYear() {
        assertThat(BirthdayCalendar.isBirthdayOn(LocalDate.of(2000, 10, 23), LocalDate.of(2026, 10, 23))).isTrue();
        assertThat(BirthdayCalendar.isBirthdayOn(LocalDate.of(2000, 10, 23), LocalDate.of(2026, 10, 24))).isFalse();
    }

    @Test
    void leapDayBirthdayIsCelebratedOnFeb28InNonLeapYears() {
        LocalDate leapBirthday = LocalDate.of(2000, 2, 29);
        assertThat(BirthdayCalendar.isBirthdayOn(leapBirthday, LocalDate.of(2026, 2, 28))).isTrue();
        assertThat(BirthdayCalendar.isBirthdayOn(leapBirthday, LocalDate.of(2028, 2, 28))).isFalse();
        assertThat(BirthdayCalendar.isBirthdayOn(leapBirthday, LocalDate.of(2028, 2, 29))).isTrue();
    }
}
