/*
 * Copyright (C) Posten Norge AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package no.digipost.time;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.List;

import static co.unruly.matchers.Java8Matchers.where;
import static java.time.Clock.systemUTC;
import static java.time.ZoneOffset.UTC;
import static java.util.Arrays.asList;
import static nl.jqno.equalsverifier.Warning.NULL_FIELDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ControllableClockTest {

    @Test
    public void controlFixedTime() {
        LocalDateTime start = LocalDateTime.of(2015, 6, 24, 12, 15);
        ControllableClock clock = ControllableClock.freezedAt(start);

        clock.timePasses((TemporalAmount) Duration.ofSeconds(45));
        assertThat(clock.zonedDateTime(), is(start.atZone(clock.getZone()).plusSeconds(45)));

        LocalDateTime aug14 = LocalDateTime.of(2016, 8, 14, 12, 00);
        clock.set(aug14);
        clock.timePasses(Period.ofDays(1));
        assertThat(clock.zonedDateTime(), is(aug14.plusDays(1).atZone(clock.getZone())));
    }

    @Test
    public void controlUsingCustomTemporalAmount() {
        TemporalAmount slack = new TemporalAmount() {
            final Duration duration = Duration.ofMinutes(15);
            @Override
            public Temporal subtractFrom(Temporal temporal) {
                throw new UnsupportedOperationException("subtractFrom() method is not supported");
            }

            @Override
            public List<TemporalUnit> getUnits() {
                return asList(ChronoUnit.SECONDS);
            }

            @Override
            public long get(TemporalUnit unit) {
                if (unit != ChronoUnit.SECONDS) {
                    throw new UnsupportedTemporalTypeException(String.valueOf(unit));
                }
                return duration.get(unit);
            }

            @Override
            public Temporal addTo(Temporal temporal) {
                return duration.addTo(temporal);
            }
        };

        LocalDateTime start = LocalDateTime.of(2015, 6, 24, 12, 15);
        ControllableClock clock = ControllableClock.freezedAt(start);
        clock.timePasses(slack);
        assertThat(clock.localDateTime(), is(LocalDateTime.of(2015, 6, 24, 12, 30)));
    }

    @Test
    public void settingFixedClockToSpecificTimeAndZone() {
        ControllableClock clock = ControllableClock.freezedAt(LocalDateTime.of(2015, 6, 24, 12, 15));

        ZonedDateTime newTime = ZonedDateTime.of(LocalDateTime.of(2014, 3, 29, 11, 0), ZoneId.of("GMT-4"));
        clock.set(newTime);
        assertThat(clock.instant(), is(newTime.toInstant()));
    }

    @Test
    public void settingTimeBasedOnSystemClock() throws InterruptedException {
        ControllableClock clock = ControllableClock.control(systemUTC());

        LocalDateTime newTime = LocalDateTime.of(2014, 3, 29, 11, 0);
        clock.set(newTime);
        Thread.sleep(200);
        LocalDateTime snapshot = LocalDateTime.ofInstant(clock.instant(), clock.getZone());
        assertThat(snapshot, greaterThan(newTime));
        assertThat(snapshot, lessThan(newTime.plusSeconds(5)));
    }

    @Test
    public void setClockToItselfIsAnError() {
        ControllableClock clock = ControllableClock.freezedAt(LocalDateTime.of(2015, 6, 24, 12, 15));
        assertThrows(IllegalArgumentException.class, () -> clock.set(previous -> clock));
    }

    @Test
    public void settingTheTimeOnSystemClockWillKeepTheClockMovingUntilFreezingTheClock() throws InterruptedException {
        ControllableClock clock = ControllableClock.control(systemUTC());
        ZonedDateTime start = ZonedDateTime.of(LocalDateTime.of(2013, 2, 17, 14, 30), ZoneId.systemDefault());
        clock.set(start);
        Thread.sleep(10);
        assertThat(clock.instant(), greaterThan(start.toInstant()));
        assertThat(clock.instant(), lessThan(start.toInstant().plusSeconds(5)));

        clock.freeze();
        Instant freezedInstant = clock.instant();
        Thread.sleep(10);
        assertThat(clock.instant(), is(freezedInstant));
    }

    @Test
    public void correctEqualsAndHashcode() {
        EqualsVerifier.forClass(ControllableClock.class)
            .suppress(NULL_FIELDS)
            .withRedefinedSuperclass()
            .verify();
    }

    @Test
    void changeToSameZoneReturnsSameInstance() {
        Clock clock = ControllableClock.freezedAt(Instant.now());
        assertThat(clock.withZone(clock.getZone()), sameInstance(clock));
    }

    @Test
    void changeToOtherZone() {
        Clock utcClock = ControllableClock.freezedAt(Instant.now(), UTC);
        ZoneId OSLO_ZONE = ZoneId.of("Europe/Oslo");
        Clock osloClock = utcClock.withZone(OSLO_ZONE);
        assertThat(utcClock, where(Clock::getZone, is(UTC)));
        assertThat(osloClock, where(Clock::getZone, is(OSLO_ZONE)));
        assertThat(utcClock, where(Clock::instant, is(osloClock.instant())));
    }

    @Test
    void successfulOperationWithAdjustedClockIsResetAfterwards() {
        ZonedDateTime initialTime = ZonedDateTime.of(2020, 1, 4, 12, 30, 0, 0, UTC);
        ControllableClock clock = ControllableClock.freezedAt(initialTime);
        clock.doWithTimeAdjusted(adjust -> adjust.timePasses(Period.ofYears(2)), twoYearsLater -> {
            assertThat(LocalDateTime.ofInstant(twoYearsLater, UTC), is(LocalDateTime.of(2022, 1, 4, 12, 30)));
            assertThat(clock.instant(), is(twoYearsLater));
        });
        assertThat(clock.instant().atZone(UTC), is(initialTime));
    }

    @Test
    void failingOperationWithAdjustedClockIsResetAfterwards() {
        ZonedDateTime initialTime = ZonedDateTime.of(2020, 1, 4, 12, 30, 0, 0, UTC);
        ControllableClock clock = ControllableClock.freezedAt(initialTime);
        assertThrows(Exception.class, () -> clock.doWithTimeAdjusted(adjust -> adjust.timePasses(Period.ofYears(2)), twoYearsLater -> {
            throw new Exception();
        }));
        assertThat(clock.instant().atZone(UTC), is(initialTime));
    }



}
