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
import java.time.*;
import java.time.temporal.TemporalAmount;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A controllable {@link Clock}, typically intended for use in testing. A {@code ControllableClock}
 * may either be freely running (as e.g. the system clock) or freezed, and can in all cases be set
 * to a new point in time.
 *
 * @see #timePasses(TemporalAmount)
 * @see #set(Instant)
 * @see #setToSystemClock()
 */
public final class ControllableClock extends Clock implements TimeControllable, Serializable {


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
        return new ControllableClock(dateTime);
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
        return new ControllableClock(dateTime);
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
        return new ControllableClock(instant);
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
        return new ControllableClock(instant, zone);
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

    /**
     * @deprecated Use {@link #freezedAt(Instant)} instead.
     */
    @Deprecated
    public ControllableClock(Instant fixedNow) {
        this(fixedNow, ZoneId.systemDefault());
    }

    /**
     * @deprecated Use {@link #freezedAt(LocalDateTime)} instead.
     */
    @Deprecated
    public ControllableClock(LocalDateTime fixedNow) {
        this(fixedNow.atZone(ZoneId.systemDefault()));
    }

    /**
     * @deprecated Use {@link #freezedAt(ZonedDateTime)} instead.
     */
    @Deprecated
    public ControllableClock(ZonedDateTime fixedNow) {
        this(fixedNow.toInstant(), fixedNow.getZone());
    }

    /**
     * @deprecated Use {@link #freezedAt(Instant, ZoneId)} instead.
     */
    @Deprecated
    public ControllableClock(Instant fixedNow, ZoneId zone) {
        this(Clock.fixed(fixedNow, zone));
    }

    /**
     * @deprecated Use {@link #control(Clock)} instead.
     */
    @Deprecated
    public ControllableClock(Clock delegate) {
        this.delegate = new AtomicReference<>(delegate);
    }

    @Override
    public ControllableClock withZone(ZoneId zone) {
        return new ControllableClock(delegate.get().withZone(zone));
    }

    @Override
    public Instant instant() {
        return delegate.get().instant();
    }

    @Override
    public ZoneId getZone() {
        return delegate.get().getZone();
    }

    @Override
    public void timePasses(TemporalAmount amountOfTime) {
        Duration duration;
        if (amountOfTime instanceof Duration) {
            duration = (Duration) amountOfTime;
        } else {
            Instant now = this.instant();
            duration = Duration.between(now, now.atZone(getZone()).plus(amountOfTime));
        }
        timePasses(duration);
    }

    @Override
    public void timePasses(Duration duration) {
        delegate.getAndUpdate(previous -> Clock.offset(previous, duration));
    }

    @Override
    public void set(LocalDateTime dateTime) {
        set(dateTime.atZone(getZone()));
    }

    @Override
    public void set(Instant newInstant) {
        delegate.getAndUpdate(previous -> Clock.offset(previous, Duration.between(previous.instant(), newInstant)));
    }

    public void set(Clock newDelegate) {
        if (this.equals(newDelegate)) {
            throw new IllegalArgumentException("Cycle detected! Tried to set " + this + " with same instance as itself!");
        }
        delegate.set(newDelegate);
    }

    @Override
    public void freeze() {
        set(Clock.fixed(delegate.get().instant(), delegate.get().getZone()));
    }

    @Override
    public void setToSystemClock() {
        setToSystemClock(getZone());
    }

    public void setToSystemClock(ZoneId zoneId) {
        set(Clock.system(zoneId));
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
