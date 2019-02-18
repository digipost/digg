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

final class ClockSnapshot {

    interface Resolver {
        /**
         * Resolves an aggregate of the Instant and ZoneId of a {@link java.time.Clock}. The
         * {@link ClockSnapshot} type is internal to the Digg library, and as such this
         * method is <strong>not</strong> part of the public API of Digg.
         *
         * @return a ClockSnapshot
         */
        ClockSnapshot clockSnapshot();
    }

    interface ResolveFromJavaClock extends Resolver {
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

    void accept(BiConsumer<Instant, ZoneId> action) {
        action.accept(instant, zone);
    }

    <T> T apply(BiFunction<Instant, ZoneId, T> action) {
        return action.apply(instant, zone);
    }
}
