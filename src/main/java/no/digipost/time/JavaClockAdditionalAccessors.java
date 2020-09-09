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
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * A {@link java.time.Clock} only exposes the current time through
 * an {@link Clock#instant() instant} and a {@link Clock#getZone() zone}.
 * Adding this interface to a {@code Clock} subclass will
 * add accessors for additional temporal types from the Java Time API.
 */
interface JavaClockAdditionalAccessors extends JavaClockAccessors {

    /**
     * Gets the current {@link ZonedDateTime} resolved with the zone of the clock.
     *
     * @return the current time as a zoned date and time.
     */
    default ZonedDateTime zonedDateTime() {
        return instant().atZone(getZone());
    }

    /**
     * Gets the current {@link LocalDateTime} resolved for the zone of the clock.
     *
     * @return the current time as a local date and time.
     */
    default LocalDateTime localDateTime() {
        return LocalDateTime.ofInstant(instant(), getZone());
    }

    /**
     * Gets the current {@link OffsetDateTime} resolved for the zone of the clock.
     *
     * @return the current time as a date and time with {@link ZoneOffset zone offset}.
     */
    default OffsetDateTime offsetDateTime() {
        return OffsetDateTime.ofInstant(instant(), getZone());
    }

}
