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

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import static java.time.Duration.between;
import static java.time.Instant.now;
import static java.util.Optional.ofNullable;

/**
 * A util for timing the duration a type of value meets a set condition. The value
 * is supplied periodically to the timer with {@link #inspect(Object) inspect(..)}, and
 * the timing starts once a supplied value matches the predicate the timer is
 * initialized with. The duration can only be retrieved as long as the condition
 * was met at least on the last invocation of {@link #inspect(Object) inspect(..)}, or
 * it returns {@link Optional#empty()}.
 */
public final class ConditionalTimer<T> {

	public static final class WithCustomClockBuilder {
		private final Clock clock;

		private WithCustomClockBuilder(Clock clock) {
			this.clock = clock;
		}

		public <T> ConditionalTimer<T> timeWhen(Predicate<T> condition) {
			return new ConditionalTimer<>(condition, clock);
		}
	}

	public static ConditionalTimer.WithCustomClockBuilder using(Clock clock) {
		return new ConditionalTimer.WithCustomClockBuilder(clock);
	}

	public static <T> ConditionalTimer<T> timeWhen(Predicate<T> condition) {
		return using(Clock.systemUTC()).timeWhen(condition);
	}


	private ConditionalTimer(Predicate<T> condition, Clock clock) {
		this.condition = condition;
		this.clock = clock;
	};


	private final Clock clock;
	private final Predicate<T> condition;
	private final AtomicReference<Instant> conditionMet = new AtomicReference<>();


	/**
	 * Perform inspection of a value to determine if timing should occur.
	 */
	public final void inspect(T value) {
		if (condition.test(value)) {
			conditionMet.compareAndSet(null, now(clock));
		} else {
			conditionMet.set(null);
		}
	}

	/**
	 * @return the duration of the currently ongoing met condition, or {@link Optional#empty()}
	 *         if the condition is currently not met.
	 */
	public Optional<Duration> getDuration() {
		return ofNullable(conditionMet.get()).map(whenMet -> between(whenMet, now(clock)));
	}

	/**
	 * @return {@code false} if condition is currently not met, {@code true} if the current {@link #getDuration() duration}
	 *         is longer than the given threshold, or {@code false} otherwise.
	 */
	public boolean longerThan(Duration threshold) {
		return getDuration().filter(duration -> duration.toMillis() > threshold.toMillis()).isPresent();
	}

	/**
	 * @return {@code true} if condition is currently not met or the current {@link #getDuration() duration} is
	 *         less than the given threshold, or {@code false} otherwise.
	 */
	public boolean sameOrlessThan(Duration threshold) {
		return !longerThan(threshold);
	}

}
