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
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A controllable {@link Clock}, mainly intended for use in testing.
 */
public final class ControllableClock extends Clock implements TimeControllable, Serializable {

    private final AtomicReference<Clock> delegate;

    public ControllableClock(Instant fixedNow) {
        this(fixedNow, ZoneId.systemDefault());
    }

    public ControllableClock(LocalDateTime fixedNow) {
        this(fixedNow.atZone(ZoneId.systemDefault()));
    }

    public ControllableClock(ZonedDateTime fixedNow) {
        this(fixedNow.toInstant(), fixedNow.getZone());
    }

    public ControllableClock(Instant fixedNow, ZoneId zone) {
        this(Clock.fixed(fixedNow, zone));
    }

    public ControllableClock(Clock delegate) {
        this.delegate = new AtomicReference<>(delegate);
    }

    @Override
    public ZoneId getZone() {
        return delegate.get().getZone();
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
