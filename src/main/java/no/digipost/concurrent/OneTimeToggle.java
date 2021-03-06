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
package no.digipost.concurrent;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * A kind of specialized variant of {@link AtomicBoolean} which
 * can only be toggled once. There is no way to "untoggle" it.
 * Successive attempts to toggle it with {@link #now()} will have no effect,
 * but it is possible to enforce a fail-fast <em>toggle only once</em> policy using
 * {@link #nowOrIfAlreadyThenThrow(Supplier)} which will throw an exception
 * if the toggle happens several times.
 *
 * @see TargetState
 */
public final class OneTimeToggle implements TargetState {

    private final AtomicBoolean toggled = new AtomicBoolean(false);

    /**
     * Toggle it! This will make {@link #yet()} return <code>true</code>.
     */
    public void now() {
        toggled.set(true);
    }

    /**
     * Toggle it, or throw exception if the toggle has already been done, for instance
     * by another thread.
     *
     * @param <E> the exception type that this method may throw.
     * @param exceptionSupplier supply exception to throw if it was already toggled.
     */
    public <E extends Throwable> void nowOrIfAlreadyThenThrow(Supplier<E> exceptionSupplier) throws E {
        E exceptionIfAlreadyToggled = toggleAndExecuteIfPreviouslyWas(true, exceptionSupplier);
        if (exceptionIfAlreadyToggled != null) {
            throw exceptionIfAlreadyToggled;
        }
    }

    @Override
    public boolean yet() {
        return toggled.get();
    }

    /**
     * Toggle it, to make {@link #yet()} return <code>true</code>, <em>and</em> if
     * a toggle was actually done (i.e. the {@code OneTimeToggle} has not already been toggled),
     * execute the provided action. This can be used to facilitate at-most-once
     * execution semantics, even in a multi-threaded context.
     *
     * @param action the action to run <em>if</em> the toggle was actually switched.
     */
    public void nowAndUnlessAlreadyToggled(Runnable action) {
        toggleAndExecuteIfPreviouslyWas(false, () -> {
            action.run();
            return null;
        });
    }

    /**
     * Toggle it, to make {@link #yet()} return <code>true</code>, <em>and</em> if
     * a toggle was actually done (i.e. the {@code OneTimeToggle} has not already been toggled),
     * use the given supplier to resolve a value. This can be used to facilitate at-most-once
     * execution semantics, even in a multi-threaded context.
     * <p>
     * The given supplier <em>may</em> resolve a {@code null} value, and in that case the return
     * value from this method will be indistinguishable from an invocation on an already toggled
     * {@code OneTimeToggle}.
     *
     * @param <T> the type of the value resolved by the given supplier.
     * @param supplier the supplier to resolve a value <em>if</em> the toggle was actually switched.
     *
     * @return An {@link Optional} containing the value from the given supplier, <em>if</em> it was
     *         executed, otherwise always {@link Optional#empty()}.
     */
    public <T> Optional<T> nowAndUnlessAlreadyToggled(Supplier<T> supplier) {
        return Optional.ofNullable(toggleAndExecuteIfPreviouslyWas(false, supplier));
    }

    private <T> T toggleAndExecuteIfPreviouslyWas(boolean existing, Supplier<T> supplier) {
        if (toggled.getAndSet(true) == existing) {
            return supplier.get();
        } else {
            return null;
        }
    }

}
