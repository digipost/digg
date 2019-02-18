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

import java.time.Instant;
import java.time.ZoneId;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * An immutable aggregate of a single resolved state from a {@link java.time.Clock},
 * containing the clock's {@link java.time.Clock#getZone() zone}, and a resolved
 * {@link java.time.Clock#instant() instant}.
 * The class is internal to the Digg library, and as such is <strong>not</strong>
 * part of the public API of Digg.
 */
final class ClockSnapshot {

    interface Resolver {
        /**
         * Resolves a {@link ClockSnapshot}.
         * The method is <strong>not</strong> part of the public API of Digg.
         *
         * @return a ClockSnapshot
         */
        ClockSnapshot clockSnapshot();
    }

    /**
     * This interface can be implemented by classes extending
     * {@link java.time.Clock} in order to become a {@link ClockSnapshot.Resolver}
     * without having to implement any methods.
     */
    interface ResolverForJavaClock extends Resolver {
        Instant instant();
        ZoneId getZone();

        @Override
        default ClockSnapshot clockSnapshot() {
            return new ClockSnapshot(instant(), getZone());
        }
    }

    final Instant instant;
    final ZoneId zone;

    ClockSnapshot(Instant instant, ZoneId zone) {
        this.instant = instant;
        this.zone = zone;
    }

    /**
     * Send the internal state <em>to</em> a {@link BiConsumer} operation.
     *
     * @param operation the operation to perform with the {@link Instant}
     *                  and {@link ZoneId zone} of this {@code ClockSnapshot}.
     */
    void to(BiConsumer<Instant, ZoneId> operation) {
        operation.accept(instant, zone);
    }

    /**
     * Transform the internal state to a result value.
     *
     * @param <T> The result type of the mapping function, and this method.
     * @param mappingFunction the function to apply to the {@link Instant} and {@link ZoneId zone}
     *                        of this {@code ClockSnapshot}.
     * @return the result of the {@code mappingFunction}.
     */
    <T> T as(BiFunction<Instant, ZoneId, T> mappingFunction) {
        return mappingFunction.apply(instant, zone);
    }
}
