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
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.function.UnaryOperator;

/**
 * "Mixin kind of" interface for subclasses of {@link java.time.Clock} (e.g. {@link ControllableClock})
 * which enables to offer the {@link ClockAdjuster} API with only needing to implement one method:
 * {@link ClockAdjuster#set(UnaryOperator)}.
 */
interface JavaClockAdjuster extends JavaClockAccessors, ClockAdjuster {

    @Override
    default void timePasses(Duration amountOfTime) {
        set(previous -> Clock.offset(previous, amountOfTime));
    }

    @Override
    default void timePasses(TemporalAmount amountOfTime) {
        Duration duration;
        if (amountOfTime instanceof Duration) {
            duration = (Duration) amountOfTime;
        } else {
            Instant now = instant();
            duration = Duration.between(now, now.atZone(getZone()).plus(amountOfTime));
        }
        timePasses(duration);
    }

    @Override
    default void freezeAt(Instant instant, ZoneId zone) {
        set(previous -> Clock.fixed(instant, zone));
    }

    @Override
    default void freezeAt(Instant instant) {
        freezeAt(instant, getZone());
    }

    @Override
    default void freezeAt(ZonedDateTime zonedDateTime) {
        freezeAt(zonedDateTime.toInstant(), zonedDateTime.getZone());
    }

    @Override
    default void freezeAt(OffsetDateTime offsetDateTime) {
        freezeAt(offsetDateTime.toInstant(), offsetDateTime.getOffset());
    }

    @Override
    default void freezeAt(LocalDateTime offsetDateTime) {
        freezeAt(offsetDateTime.atZone(getZone()));
    }

    @Override
    default void freeze() {
        freezeAt(instant(), getZone());
    }

    @Override
    default void freezeTruncatedTo(TemporalUnit unit) {
        freezeAt(instant().truncatedTo(unit), getZone());
    }

    @Override
    default void setToSystemClock(ZoneId zoneId) {
        set(previous -> Clock.system(zoneId));
    }

    @Override
    default void setToSystemClock() {
        setToSystemClock(getZone());
    }

    @Override
    default void set(Instant instant, ZoneId zone) {
        set(previous -> Clock.offset(previous, Duration.between(previous.instant(), instant)).withZone(zone));
    }

    @Override
    default void set(Instant instant) {
        set(instant, getZone());
    }

    @Override
    default void set(ZonedDateTime zonedDateTime) {
        set(zonedDateTime.toInstant(), zonedDateTime.getZone());
    }

    @Override
    default void set(OffsetDateTime offsetDateTime) {
        set(offsetDateTime.toInstant(), offsetDateTime.getOffset());
    }

    @Override
    default void set(LocalDateTime localDateTime) {
        set(localDateTime.atZone(getZone()));
    }

}
