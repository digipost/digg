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
import org.junit.Test;
import org.junit.contrib.theories.Theories;
import org.junit.contrib.theories.Theory;
import org.junit.runner.RunWith;

import java.time.Duration;
import java.time.LocalDateTime;

import static java.time.Duration.ofSeconds;
import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.util.Optional.empty;
import static no.digipost.time.ConditionalTimer.timeWhen;
import static no.digipost.time.ConditionalTimer.using;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.*;

@RunWith(Theories.class)
public class ConditionalTimerTest {

	@Test
	public void aNewConditionalTimerYieldsNoDuration() {
		ConditionalTimer<?> timeFromFirstInspection = timeWhen(v -> true);
		assertThat(timeFromFirstInspection.getDuration(), is(empty()));
	}

	@Theory
	public void comparingDurationsForConditionsMet(@ForAll(sampleSize=10) boolean conditionMet, @ForAll long ms) {
		ControllableClock clock = new ControllableClock(now());
		ConditionalTimer<Object> timer = using(clock).timeWhen(v -> conditionMet);
		Duration duration = Duration.of(ms, MILLIS);
		assertTrue(timer.sameOrlessThan(duration));
		assertFalse(timer.longerThan(duration));
		timer.inspect(42);
		clock.timePasses(duration.minus(1, MILLIS));
		assertThat(timer.sameOrlessThan(duration), not(timer.longerThan(duration)));
		clock.timePasses(Duration.of(1, MILLIS));
		assertThat(timer.sameOrlessThan(duration), not(timer.longerThan(duration)));
		clock.timePasses(Duration.of(1, MILLIS));
		assertThat(timer.sameOrlessThan(duration), not(timer.longerThan(duration)));
	}


	@Test
	public void yieldsDurationWhenConditionIsMet() {
		ControllableClock clock = new ControllableClock(LocalDateTime.of(2015, 6, 24, 12, 15));
		ConditionalTimer<Integer> timeWhenNegative = using(clock).timeWhen(i -> i < 0);
		clock.timePasses(ofSeconds(1));
		timeWhenNegative.inspect(0);
		clock.timePasses(ofSeconds(1));
		timeWhenNegative.inspect(-1);
		clock.timePasses(ofSeconds(1));
		timeWhenNegative.inspect(-42);
		clock.timePasses(ofSeconds(1));
		assertThat(timeWhenNegative.getDuration().get(), is(Duration.ofSeconds(2)));
	}

	@Test
	public void whenInspectingAndConditionIsNotMetTheDurationIsReset() {
		ConditionalTimer<Integer> timeWhenNegative = timeWhen(i -> i < 0);
		timeWhenNegative.inspect(-1);
		assertTrue(timeWhenNegative.getDuration().isPresent());
		timeWhenNegative.inspect(1);
		assertFalse(timeWhenNegative.getDuration().isPresent());
	}
}
