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

import no.digipost.function.ThrowingConsumer;
import no.digipost.function.ThrowingFunction;

import java.io.Serializable;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAmount;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * A controllable {@link Clock}, typically intended for use in testing. A {@code ControllableClock}
 * may either be freely running (as e.g. the system clock) or freezed, and can in all cases be set
 * to a new point in time.
 *
 * @see #timePasses(TemporalAmount)
 * @see #set(Instant)
 * @see #setToSystemClock()
 */
public final class ControllableClock extends Clock implements TimeControllable, ClockSnapshot.ResolveFromJavaClock, Serializable {


    /**
     * Create a new controllable clock freezed at the instant resolved from a given
     * {@link LocalDateTime} and the <em>{@link ZoneId#systemDefault() default time zone}</em>.
     *
     * The clock will not progress on its own, and will yield the {@link #instant() instant}
     * it is set to.
     *
     * @param dateTime The date and time to set for the new freezed clock.
     * @return the new {@code ControllableClock}
     */
    public static ControllableClock freezedAt(LocalDateTime dateTime) {
        return freezedAt(dateTime.atZone(ZoneId.systemDefault()));
    }


    /**
     * Create a new controllable clock freezed at the instant of a given {@link ZonedDateTime}.
     *
     * The clock will not progress on its own, and will yield the {@link #instant() instant}
     * it is set to.
     *
     * @param dateTime The date and time to set for the new freezed clock.
     * @return the new {@code ControllableClock}
     */
    public static ControllableClock freezedAt(ZonedDateTime dateTime) {
        return freezedAt(dateTime.toInstant(), dateTime.getZone());
    }


    /**
     * Create a new controllable clock freezed at a given instant,
     * and with the <em>{@link ZoneId#systemDefault() default time zone}</em>.
     *
     * The clock will not progress on its own, and will yield the {@link #instant() instant}
     * it is set to.
     *
     * @param instant The instant to set for the new freezed clock.
     * @return the new {@code ControllableClock}
     */
    public static ControllableClock freezedAt(Instant instant) {
        return freezedAt(instant, ZoneId.systemDefault());
    }


    /**
     * Create a new controllable clock freezed at a given {@link Instant}, and with a given {@link ZoneId}.
     * The clock will not progress on its own, and will yield a set {@link #instant() instant}.
     *
     * @param instant The instant to set for the new freezed clock.
     * @param zone The time zone of the new freezed clock.
     * @return the new {@code ControllableClock}
     */
    public static ControllableClock freezedAt(Instant instant, ZoneId zone) {
        return control(Clock.fixed(instant, zone));
    }


    /**
     * Create a controllable clock based on an existing clock. The new
     * {@link ControllableClock} will have the same behavior (freezed or progressing)
     * and time as the given clock, but may be mutated to yield another {@link #instant() instant}, or
     * be {@link #freeze() freezed}. The given clock is of course not altered.
     *
     * @param clock The clock to base the new {@code ControllableClock} instance on.
     * @return the new {@code ControllableClock}
     */
    public static ControllableClock control(Clock clock) {
        return new ControllableClock(clock);
    }



    private static final long serialVersionUID = 1L;

    private final AtomicReference<Clock> delegate;

    private ControllableClock(Clock delegate) {
        this.delegate = new AtomicReference<>(delegate);
    }

    @Override
    public ControllableClock withZone(ZoneId zone) {
        Clock currentDelegate = delegate.get();
        if (zone.equals(currentDelegate.getZone())) {
            return this;
        }
        return new ControllableClock(currentDelegate.withZone(zone));
    }

    @Override
    public Instant instant() {
        return delegate.get().instant();
    }

    @Override
    public ZoneId getZone() {
        return delegate.get().getZone();
    }

    /**
     * Gets the current {@link ZonedDateTime} resolved with the zone of the clock.
     *
     * @return the current time as a zoned date and time.
     */
    public ZonedDateTime zonedDateTime() {
        return instant().atZone(getZone());
    }

    /**
     * Gets the current {@link LocalDateTime} resolved for the zone of the clock.
     *
     * @return the current time as a local date and time.
     */
    public LocalDateTime localDateTime() {
        return LocalDateTime.ofInstant(instant(), getZone());
    }

    /**
     * Perform an action with the clock adjusted, and have the clock reset to it's original state
     * after the action has finished.
     *
     * @param adjustClock how to adjust the clock before running the action
     * @param action the action to perform, which is given an instant resolved from the adjusted clock
     * @param <X> Exception the may be thrown from the given action
     *
     * @throws X if the given action throws an exception
     */
    public <X extends Exception> void doWithTimeAdjusted(Consumer<TimeControllable> adjustClock, ThrowingConsumer<Instant, X> action) throws X {
        getWithTimeAdjusted(adjustClock, time -> {
            action.accept(time);
            return null;
        });
    }

    /**
     * Resolve a value with the clock adjusted, and have the clock reset to it's original state
     * after the operation has finished.
     *
     * @param adjustClock how to adjust the clock before running the action
     * @param resolveValue the operation which resolves the value, which is given an instant resolved from the adjusted clock
     * @param <T> The returned type
     * @param <X> Exception the may be thrown from the given function
     *
     * @return the value returned from the given {@code resolveValue} function
     * @throws X if the function throws an exception while resolving the value.
     */
    public <T, X extends Exception> T getWithTimeAdjusted(Consumer<TimeControllable> adjustClock, ThrowingFunction<Instant, T, X> resolveValue) throws X {
        Clock originalClock = delegate.get();
        try {
            adjustClock.accept(this);
            return resolveValue.apply(this.instant());
        } finally {
            set(temporary -> originalClock);
        }
    }

    @Override
    public void set(UnaryOperator<Clock> createNewClock) {
        delegate.getAndUpdate(previous -> {
            Clock newClock = createNewClock.apply(previous);
            if (ControllableClock.this.equals(newClock)) {
                throw new IllegalArgumentException("Cycle detected! Tried to set " + this + " with same instance as itself!");
            }
            return newClock;
        });
    }

    @Override
    public String toString() {
        return "Controllable " + delegate.get();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ControllableClock) {
            ControllableClock that = (ControllableClock) obj;
            return Objects.equals(this.delegate.get(), that.delegate.get());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(delegate.get());
    }

}
