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

import java.time.*;
import java.time.temporal.TemporalAmount;

/**
 * The clock <em>mutation</em> operations, e.g. offered by a {@link ControllableClock}.
 * This interface can for instance be used to easily expose time manipulation through another API,
 * but you do not want to expose a {@link Clock} instance.
 */
public interface TimeControllable {

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
    default void set(ZonedDateTime zonedDateTime) {
        set(zonedDateTime.toInstant());
    }

    /**
     * Set the time to the given local date and time.
     *
     * @param localDateTime the date and time to set.
     */
    void set(LocalDateTime localDateTime);

    /**
     * Signal that time is passing a given amount of time.
     *
     * @param amountOfTime the amount of time which are passing.
     */
    void timePasses(TemporalAmount amountOfTime);

    /**
     * Signal that time is passing a given amount of time.
     *
     * @param amountOfTime the amount of time which are passing.
     */
    void timePasses(Duration duration);

    /**
     * Signal that time should freeze.
     */
    void freeze();

    /**
     * Set the time to freely progressing system time.
     *
     * @see Clock#system(ZoneId)
     */
    void setToSystemClock();

}
