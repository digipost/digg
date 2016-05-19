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

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static java.time.Duration.ZERO;
import static java.time.Duration.between;
import static java.time.Instant.MIN;
import static java.util.Objects.hash;
import static no.digipost.DiggCompare.max;
import static no.digipost.DiggCompare.min;

/**
 * A time span is the composition of two instants on the time line,
 * denoting the start (inclusively) and end (exclusively) of an amount of time
 * on the actual time-line. For simplicity, it is not valid to construct
 * time spans which are negative, that goes "against time", where the
 * start instant is after the end instant.
 */
public final class TimeSpan implements Comparable<TimeSpan>, Serializable {

    /**
     * Start constructing a new {@code TimeSpan} starting at the given {@link Instant}.
     * Use the returned {@link TimeSpan.Started#until(Instant)} or
     * {@link TimeSpan.Started#lasting(Duration)} to set the end.
     *
     * @param start The start instant of the new {@code TimeSpan}.
     *
     * @return {@link TimeSpan.Started}
     */
    public static TimeSpan.Started from(Instant start) {
        return new TimeSpan.Started(start);
    }


    /**
     * A "started" time span, which works as a simple builder
     * for a complete {@code TimeSpan}, using the concluding methods
     * {@link #until(Instant)} or {@link #lasting(Duration)}
     */
    public static final class Started {
        public final Instant at;

        private Started(Instant at) {
            this.at = at;
        }

        public TimeSpan until(Instant endExclusively) {
            if (at.isAfter(endExclusively)) {
                throw new IllegalArgumentException("start instant " + at + " is after end instant " + endExclusively);
            }
            return new TimeSpan(at, endExclusively, null);
        }

        public TimeSpan lasting(Duration duration) {
            return new TimeSpan(at, null, duration);
        }
    }


    private TimeSpan(Instant start, Instant end, Duration duration) {
        this.start = start;
        this.end = end != null ? end : start.plus(duration);
        this.duration = duration != null ? duration : between(this.start, this.end);
    }



    public static final TimeSpan EMPTY_FAR_PAST = TimeSpan.from(MIN).lasting(ZERO);


    private static final long serialVersionUID = 1L;

    public final Instant start;
    public final Instant end;
    public final Duration duration;




    /**
     * Collapse to <em>one or two</em> spans. If the given {@link TimeSpan} <em>overlaps</em>
     * with this, they are merged together to a new {@code TimeSpan} instance which covers
     * the effective duration of the two spans. If the given {@code TimeSpan} does
     * not overlap with this, the two spans are returned in chronological order,
     * the earlier one placed first.
     *
     * @param other The other TimeSpan to collapse this with.
     * @return The list containing the collapsed TimeSpan, <em>or</em> this
     *         and the other if the two spans do not overlap each other.
     */
    public Stream<TimeSpan> collapse(TimeSpan other) {
        Optional<TimeSpan> intersection = this.intersection(other);
        if (intersection.isPresent()) {
            return Stream.of(TimeSpan
                    .from(min(this.start, other.start))
                    .lasting(this.duration.plus(other.duration).minus(intersection.map(s -> s.duration).orElse(ZERO))));
        } else {
            return Stream.of(this, other).sorted();
        }
    }


    /**
     * Give the intersection (the overlapping part) of this
     * and the given {@code TimeSpan}, or {@link Optional#empty()}
     * if they do not overlap.
     */
    public Optional<TimeSpan> intersection(TimeSpan other) {
        TimeSpan earliest = min(this, other);
        TimeSpan latest = max(this, other);
        if (earliest.includes(latest.start)) {
            if (earliest.covers(latest)) {
                return Optional.of(latest);
            } else {
                return Optional.of(TimeSpan.from(latest.start).until(min(earliest.end, latest.end)));
            }
        } else {
            return Optional.empty();
        }
    }


    /**
     * Check if an {@link Instant} is part of this time span. Includes start
     * instant and excludes end instant of this time span.
     *
     * @return {@code true} if the given {@code instant} is part of this time span,
     *         {@code false} otherwise.
     */
    public boolean includes(Instant instant) {
        return instant.equals(start) || (instant.isAfter(start) && instant.isBefore(end));
    }

    /**
     * Check if the given TimeSpan is completly covered by this.
     *
     * @return {@code true} if the given {@code TimeSpan} is covered by this,
     *         {@code false} otherwise.
     */
    public boolean covers(TimeSpan other) {
        return !other.start.isBefore(this.start) && !other.end.isAfter(this.end);
    }

    @Override
    public int compareTo(TimeSpan other) {
        int comparison = this.start.compareTo(other.start);
        return (comparison != 0) ? comparison : this.end.compareTo(other.end);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof TimeSpan) {
            TimeSpan other = (TimeSpan) o;
            return Objects.equals(this.start, other.start) &&
                   Objects.equals(this.end, other.end) &&
                   Objects.equals(this.duration, other.duration);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return hash(start, end, duration);
    }

    @Override
    public String toString() {
        return start + " to " + end + " (duration: " + duration + ")";
    }

}
