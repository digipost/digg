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

import org.junit.jupiter.api.Test;
import org.quicktheories.WithQuickTheories;
import org.quicktheories.core.Gen;

import java.time.Duration;
import java.time.LocalDateTime;

import static co.unruly.matchers.Java8Matchers.where;
import static co.unruly.matchers.OptionalMatchers.contains;
import static co.unruly.matchers.OptionalMatchers.empty;
import static java.time.Duration.ofSeconds;
import static no.digipost.time.ConditionalTimer.timeWhen;
import static no.digipost.time.ConditionalTimer.using;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;


public class ConditionalTimerTest implements WithQuickTheories {

    @Test
    public void aNewConditionalTimerYieldsNoDuration() {
        ConditionalTimer<?> timeFromFirstInspection = timeWhen(v -> true);
        assertThat(timeFromFirstInspection.getDuration(), is(empty()));
    }

    final ControllableClock clock = ControllableClock.freezedAt(LocalDateTime.of(2015, 6, 24, 12, 15));

    @Test
    public void comparingDurationsForConditionsMet() {
        Gen<ConditionalTimer<Object>> timers = arbitrary().pick(true, false, true, true).map(conditionMet -> using(clock).timeWhen(v -> conditionMet));
        Gen<Duration> durations = integers().all().map(Duration::ofMillis);

        qt()
            .forAll(timers, durations)
            .check((timer, duration) -> {
                clock.timePasses(duration);
                return timer.sameOrlessThan(duration) && !timer.longerThan(duration);
            });


        qt()
            .forAll(timers, durations)
            .check((timer, duration) -> inspectAndPassTime(timer, duration.minusMillis(1)).sameOrlessThan(duration));


        qt()
            .forAll(timers, durations)
            .check((timer, duration) -> inspectAndPassTime(timer, duration).sameOrlessThan(duration));

        qt()
            .forAll(timers, durations)
            .assuming((timer, duration) -> inspectAndPassTime(timer, duration.plusMillis(1)).getDuration().isPresent())
            .check(ConditionalTimer::longerThan);

    }

    private ConditionalTimer<Object> inspectAndPassTime(ConditionalTimer<Object> timer, Duration duration) {
        timer.inspect(42);
        clock.timePasses(duration);
        return timer;
    }


    @Test
    public void yieldsDurationWhenConditionIsMet() {
        ControllableClock clock = ControllableClock.freezedAt(LocalDateTime.of(2015, 6, 24, 12, 15));
        ConditionalTimer<Integer> timeWhenNegative = using(clock).timeWhen(i -> i < 0);
        clock.timePasses(ofSeconds(1));
        timeWhenNegative.inspect(0);
        clock.timePasses(ofSeconds(1));
        timeWhenNegative.inspect(-1);
        clock.timePasses(ofSeconds(1));
        timeWhenNegative.inspect(-42);
        clock.timePasses(ofSeconds(1));
        assertThat(timeWhenNegative, where(ConditionalTimer::getDuration, contains(Duration.ofSeconds(2))));
    }

    @Test
    public void whenInspectingAndConditionIsNotMetTheDurationIsReset() {
        ConditionalTimer<Integer> timeWhenNegative = timeWhen(i -> i < 0);
        timeWhenNegative.inspect(-1);
        assertThat(timeWhenNegative, where(ConditionalTimer::getDuration, not(empty())));
        timeWhenNegative.inspect(1);
        assertThat(timeWhenNegative, where(ConditionalTimer::getDuration, empty()));
    }
}
