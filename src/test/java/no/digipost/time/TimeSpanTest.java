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

import nl.jqno.equalsverifier.EqualsVerifier;
import no.digipost.time.TimeSpan.Started;
import org.junit.jupiter.api.Test;
import org.quicktheories.WithQuickTheories;
import org.quicktheories.api.Pair;
import org.quicktheories.core.Gen;

import java.time.Duration;
import java.time.Instant;
import java.util.stream.Stream;

import static uk.co.probablyfine.matchers.StreamMatchers.contains;
import static uk.co.probablyfine.matchers.StreamMatchers.equalTo;
import static java.time.Duration.ofMinutes;
import static java.time.Duration.ofSeconds;
import static java.time.Instant.now;
import static java.time.Period.ofDays;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TimeSpanTest implements WithQuickTheories {

    @Test
    public void notValidToConstructNegativeTimeSpan() {
        Instant now = Instant.now();
        Instant thirtySecondsAgo = now.minusSeconds(30);
        Started timeSpanFromNow = TimeSpan.from(now);
        assertThrows(IllegalArgumentException.class, () -> timeSpanFromNow.until(thirtySecondsAgo));
    }

    @Test
    public void hasCorrectEqualsAndHashCode() {
        EqualsVerifier.forClass(TimeSpan.class).verify();
    }

    @Test
    public void orderByStartOfSpan() {
        TimeSpan fiveMinsYesterday = TimeSpan.from(now().minus(ofDays(1))).lasting(ofMinutes(10));
        TimeSpan fiveMinsToday = TimeSpan.from(now()).lasting(ofMinutes(5));

        assertThat(Stream.of(fiveMinsToday, fiveMinsYesterday).sorted(), contains(fiveMinsYesterday, fiveMinsToday));
    }

    @Test
    public void spansBeginningOnTheSameTimeAreOrderedByTheirLength() {
        Instant start = now();
        TimeSpan tenMins = TimeSpan.from(start).lasting(ofMinutes(10));
        TimeSpan fiveMins = TimeSpan.from(start).lasting(ofMinutes(5));

        assertThat(Stream.of(tenMins, fiveMins).sorted(), contains(fiveMins, tenMins));
    }

    @Test
    public void toStringIncludesStartAndEndAndDuration() {
        Instant start = now();
        Instant end = start.plusSeconds(60);
        String span = TimeSpan.from(start).until(end).toString();
        assertThat(span, containsString(start.toString()));
        assertThat(span, containsString(end.toString()));
        assertThat(span, containsString(Duration.between(start, end).toString()));
    }


    @Test
    public void collapsingTwoAdjacentTimeSpansWillKeepTheOriginalSpansInChronologicalOrder() {
        Instant start = now();
        TimeSpan first = TimeSpan.from(start).lasting(ofSeconds(10));
        TimeSpan second = TimeSpan.from(first.end).lasting(ofSeconds(10));
        assertThat(second.collapse(first), contains(first, second));
        assertThat(first.collapse(second), contains(first, second));
    }

    @Test
    public void collapsingTwoNonOverlappingTimeSpansWillKeepTheOriginalSpansInChronologicalOrder() {
        Instant start = now();
        TimeSpan first = TimeSpan.from(start).until(start.plusSeconds(10));
        Instant start2 = first.end.plusSeconds(10);
        TimeSpan second = TimeSpan.from(start2).until(start2.plusSeconds(10));
        assertThat(first.collapse(second), contains(first, second));
        assertThat(second.collapse(first), contains(first, second));
    }

    @Test
    public void collapsingAnOverlappingTimeSpanWillYieldANewSpanCoveringTheTwoInitialSpans() {
        Instant start = now();
        TimeSpan first = TimeSpan.from(start).lasting(ofSeconds(10));
        Instant start2 = start.plusSeconds(2);
        TimeSpan second = TimeSpan.from(start2).lasting(ofSeconds(10));
        assertThat(first.collapse(second), contains(TimeSpan.from(start).lasting(ofSeconds(12))));
        assertThat(second.collapse(first), contains(TimeSpan.from(start).lasting(ofSeconds(12))));
    }

    @Test
    public void collapsingASpanCoveringAnotherWillYieldOnlyTheCoveringSpan() {
        Instant start = now();
        TimeSpan firstSpan = TimeSpan.from(start).lasting(ofSeconds(10));
        TimeSpan secondSpan = TimeSpan.from(start.plusSeconds(2)).lasting(Duration.ofSeconds(2));
        assertThat(firstSpan.collapse(secondSpan), contains(firstSpan));
        assertThat(secondSpan.collapse(firstSpan), contains(firstSpan));
    }

    @Test
    public void collapsingIsCommutative() {
        Gen<TimeSpan> timeSpans = integers().all()
            .map(Instant.now()::plusSeconds)
            .zip(integers().allPositive().map(Duration::ofMillis), (start, duration) -> TimeSpan.from(start).lasting(duration));

        qt()
            .forAll(timeSpans.map(Pair::of))
            .checkAssert(spans -> assertThat(spans._1.collapse(spans._2), equalTo(spans._2.collapse(spans._1))));
    }

}
