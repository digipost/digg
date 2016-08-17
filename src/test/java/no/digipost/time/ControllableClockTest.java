/**
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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.time.*;
import java.time.temporal.*;
import java.util.List;

import static java.time.Clock.systemUTC;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class ControllableClockTest {

    @Test
    public void controlFixedTime() {
        LocalDateTime start = LocalDateTime.of(2015, 6, 24, 12, 15);
        ControllableClock clock = new ControllableClock(start);

        clock.timePasses((TemporalAmount) Duration.ofSeconds(45));
        assertThat(clock.instant(), is(start.atZone(clock.getZone()).plusSeconds(45).toInstant()));

        LocalDateTime aug14 = LocalDateTime.of(2016, 8, 14, 12, 00);
        clock.set(aug14);
        clock.timePasses(Period.ofDays(1));
        assertThat(clock.instant(), is(aug14.plusDays(1).atZone(clock.getZone()).toInstant()));
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
        ControllableClock clock = new ControllableClock(start);
        clock.timePasses(slack);
        assertThat(clock.instant(), is(LocalDateTime.of(2015, 6, 24, 12, 30).atZone(clock.getZone()).toInstant()));
    }

    @Test
    public void settingFixedClockToSpecificTimeAndZone() {
        ControllableClock clock = new ControllableClock(LocalDateTime.of(2015, 6, 24, 12, 15));

        ZonedDateTime newTime = ZonedDateTime.of(LocalDateTime.of(2014, 3, 29, 11, 0), ZoneId.of("GMT-4"));
        clock.set(newTime);
        assertThat(clock.instant(), is(newTime.toInstant()));
    }

    @Test
    public void settingTimeBasedOnSystemClock() throws InterruptedException {
        ControllableClock clock = new ControllableClock(systemUTC());

        LocalDateTime newTime = LocalDateTime.of(2014, 3, 29, 11, 0);
        clock.set(newTime);
        Thread.sleep(200);
        LocalDateTime snapshot = LocalDateTime.ofInstant(clock.instant(), clock.getZone());
        assertThat(snapshot, greaterThan(newTime));
        assertThat(snapshot, lessThan(newTime.plusSeconds(5)));
    }

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void setClockToItselfIsAnError() {
        ControllableClock clock = new ControllableClock(LocalDateTime.of(2015, 6, 24, 12, 15));
        expectedException.expect(IllegalArgumentException.class);
        clock.set(clock);
    }

    @Test
    public void settingTheTimeOnSystemClockWillKeepTheClockMovingUntilFreezingTheClock() throws InterruptedException {
        ControllableClock clock = new ControllableClock(systemUTC());
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

}
