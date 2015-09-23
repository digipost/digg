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

import com.pholser.junit.quickcheck.ForAll;
import com.pholser.junit.quickcheck.From;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.theories.Theories;
import org.junit.contrib.theories.Theory;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.time.Duration;
import java.time.Instant;
import java.util.stream.Stream;

import static co.unruly.matchers.StreamMatchers.contains;
import static co.unruly.matchers.StreamMatchers.equalTo;
import static java.time.Duration.ofMinutes;
import static java.time.Duration.ofSeconds;
import static java.time.Instant.now;
import static java.time.Period.ofDays;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

@RunWith(Theories.class)
public class TimeSpanTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void notValidToConstructNegativeTimeSpan() {
        Instant now = Instant.now();
        Instant thirtySecondsAgo = now.minusSeconds(30);
        expectedException.expect(IllegalArgumentException.class);
        TimeSpan.from(now).until(thirtySecondsAgo);
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

    @Theory
    public void collapsingIsCommutative(@ForAll @From(RandomTimeSpans.class) TimeSpan someSpan, @ForAll @From(RandomTimeSpans.class) TimeSpan someOtherSpan) {
        assertThat(someSpan.collapse(someOtherSpan), equalTo(someOtherSpan.collapse(someSpan)));
    }

}
