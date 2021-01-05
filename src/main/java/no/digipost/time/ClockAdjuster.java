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

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.function.UnaryOperator;

/**
 * A clock <em>adjusting</em> API, e.g. offered by a {@link ControllableClock}.
 * This interface can for instance be used to easily expose time manipulation through another API,
 * but you do not want to expose a {@link Clock} or {@code ControllableClock} instance.
 */
public interface ClockAdjuster {

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
    void timePasses(Duration amountOfTime);


    /**
     * Signal that time is passing a given amount of time.
     *
     * @param amountOfTime the amount of time which are passing.
     */
    void timePasses(TemporalAmount amountOfTime);


    /**
     * Freeze the clock at given instant and {@link ZoneId zone}.
     */
    void freezeAt(Instant instant, ZoneId zone);


    /**
     * Freeze the clock at given instant.
     */
    void freezeAt(Instant instant);


    /**
     * Freeze the clock at given date and time.
     */
    void freezeAt(ZonedDateTime zonedDateTime);


    /**
     * Freeze the clock at given date and time.
     */
    void freezeAt(OffsetDateTime offsetDateTime);


    /**
     * Freeze the clock at given date and time.
     */
    void freezeAt(LocalDateTime offsetDateTime);


    /**
     * Freeze the clock at its current instant in time.
     */
    void freeze();


    /**
     * Freeze the clock at its current instant in time, which will be truncated
     * to the given unit.
     *
     * @param unit the unit the freezed instant will be truncated to
     *
     * @see ChronoUnit
     * @see Instant#truncatedTo(TemporalUnit)
     */
    void freezeTruncatedTo(TemporalUnit unit);


    /**
     * Set the time to freely progressing system time with
     * the given zone.
     *
     * @see Clock#system(ZoneId)
     */
    void setToSystemClock(ZoneId zoneId);


    /**
     * Set the time to freely progressing system time.
     *
     * @see Clock#system(ZoneId)
     */
    void setToSystemClock();


    /**
     * Set the time of the clock to the given instant and zone.
     *
     * @param instant the instant to set.
     * @param zone the zone to set.
     */
    void set(Instant instant, ZoneId zone);


    /**
     * Set the time to the given instant.
     *
     * @param instant the instant to set.
     */
    void set(Instant instant);


    /**
     * Set the time to the given <em>zoned</em> date and time.
     *
     * @param zonedDateTime the date and time to set.
     */
    void set(ZonedDateTime zonedDateTime);


    /**
     * Set the time to the given <em>offset</em> date and time.
     *
     * @param offsetDateTime the date and time to set.
     */
    void set(OffsetDateTime offsetDateTime);


    /**
     * Set the time to the given local date and time.
     *
     * @param localDateTime the date and time to set.
     */
    void set(LocalDateTime localDateTime);

}
