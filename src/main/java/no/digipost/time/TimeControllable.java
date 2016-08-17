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

    void set(Instant time);

    default void set(ZonedDateTime zonedDateTime) {
        set(zonedDateTime.toInstant());
    }

    void set(LocalDateTime localDateTime);

    void timePasses(TemporalAmount amountOfTime);

    void timePasses(Duration duration);

    void freeze();

    void setToSystemClock();

}
