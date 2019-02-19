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

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAmount;
import java.util.function.UnaryOperator;

/**
 * The clock <em>mutation</em> operations, e.g. offered by a {@link ControllableClock}.
 * This interface can for instance be used to easily expose time manipulation through another API,
 * but you do not want to expose a {@link Clock} instance.
 */
public interface TimeControllable extends ClockSnapshot.Resolver {

    /**
     * Set a new clock to resolve the time from.
     *
     * @param createNewClock function which is given the current clock, which may
     *                       be used to create a new clock.
     */
    void set(UnaryOperator<Clock> createNewClock);


    /**
     * Signal that time is passing a given amount of time.
     *
     * @param amountOfTime the amount of time which are passing.
     */
    default void timePasses(Duration amountOfTime) {
        set(previous -> Clock.offset(previous, amountOfTime));
    }

    /**
     * Signal that time is passing a given amount of time.
     *
     * @param amountOfTime the amount of time which are passing.
     */
    default void timePasses(TemporalAmount amountOfTime) {
        Duration duration;
        if (amountOfTime instanceof Duration) {
            duration = (Duration) amountOfTime;
        } else {
            duration = clockSnapshot().as((instant, zone) -> Duration.between(instant, instant.atZone(zone).plus(amountOfTime)));
        }
        timePasses(duration);
    }


    /**
     * Freeze the clock at given instant and {@link ZoneId zone}.
     */
    default void freezeAt(Instant instant, ZoneId zone) {
        set(previous -> Clock.fixed(instant, zone));
    }

    /**
     * Freeze the clock at given instant.
     */
    default void freezeAt(Instant instant) {
        freezeAt(instant, clockSnapshot().zone);
    }

    /**
     * Freeze the clock at given date and time.
     */
    default void freezeAt(ZonedDateTime zonedDateTime) {
        freezeAt(zonedDateTime.toInstant(), zonedDateTime.getZone());
    }

    /**
     * Freeze the clock at given date and time.
     */
    default void freezeAt(OffsetDateTime offsetDateTime) {
        freezeAt(offsetDateTime.toInstant(), offsetDateTime.getOffset());
    }

    /**
     * Freeze the clock at given date and time.
     */
    default void freezeAt(LocalDateTime offsetDateTime) {
        freezeAt(offsetDateTime.atZone(clockSnapshot().zone));
    }

    /**
     * Signal that the clock should freeze at the instant it is currently at.
     */
    default void freeze() {
        clockSnapshot().to(this::freezeAt);
    }


    /**
     * Set the time to freely progressing system time with
     * the given zone.
     *
     * @see Clock#system(ZoneId)
     */
    default void setToSystemClock(ZoneId zoneId) {
        set(previous -> Clock.system(zoneId));
    }

    /**
     * Set the time to freely progressing system time.
     *
     * @see Clock#system(ZoneId)
     */
    default void setToSystemClock() {
        setToSystemClock(clockSnapshot().zone);
    }


    /**
     * Set the time of the clock to the given instant and zone.
     *
     * @param instant the instant to set.
     * @param zone the zone to set.
     */
    default void set(Instant instant, ZoneId zone) {
        set(previous -> Clock.offset(previous, Duration.between(previous.instant(), instant)).withZone(zone));
    }

    /**
     * Set the time to the given instant.
     *
     * @param instant the instant to set.
     */
    default void set(Instant instant) {
        set(instant, clockSnapshot().zone);
    }

    /**
     * Set the time to the given <em>zoned</em> date and time.
     *
     * @param zonedDateTime the date and time to set.
     */
    default void set(ZonedDateTime zonedDateTime) {
        set(zonedDateTime.toInstant(), zonedDateTime.getZone());
    }

    /**
     * Set the time to the given <em>offset</em> date and time.
     *
     * @param offsetDateTime the date and time to set.
     */
    default void set(OffsetDateTime offsetDateTime) {
        set(offsetDateTime.toInstant(), offsetDateTime.getOffset());
    }


    /**
     * Set the time to the given local date and time.
     *
     * @param localDateTime the date and time to set.
     */
    default void set(LocalDateTime localDateTime) {
        set(localDateTime.atZone(clockSnapshot().zone));
    }

}
