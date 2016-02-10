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
package no.digipost.concurrent;

import no.digipost.util.Assignment;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * A reference which may or may not be assigned a value, with the added constraint
 * that it can only be assigned once. If it is assigned a value more than one time
 * it will throw an exception.
 * <p>
 * The class is thread-safe in the sense that it is not possible for concurrent threads to
 * assign a value to it twice, though relevant cases for concurrent use are probably limited. The
 * motivation is rather to enable fail-fast for erroneous API-usage involving assignments,
 * e.g. builder implementations.
 *
 *
 * @param <V> The type of the object which is referenced.
 */
public final class OneTimeAssignment<V> implements Assignment<V> {

    /**
     * Create a new non-assigned instance with a default value.
     *
     * @param defaultValue The default value to use in case an assignment
     *                     through {@link #set(Object)} is never performed.
     * @see #defaultTo(Supplier)
     */
    public static final <V> OneTimeAssignment<V> defaultTo(V defaultValue) {
        return defaultTo(() -> defaultValue);
    }

    /**
     * Create a new non-assigned instance with a default value.
     *
     * @param defaultValue The supplier to acquire the default value to use in case an
     *                     assignment through {@link #set(Object)} is never performed.
     */
    public static final <V> OneTimeAssignment<V> defaultTo(Supplier<? extends V> defaultValue) {
        return new OneTimeAssignment<>(defaultValue);
    }

    /**
     * Create a new non-assigned instance.
     */
    public static final <V> OneTimeAssignment<V> newInstance() {
        return new OneTimeAssignment<V>(() -> UNASSIGNED);
    }



    private final AtomicReference<Object> ref = new AtomicReference<>(UNASSIGNED);
    private final Supplier<?> defaultValue;

    private OneTimeAssignment(Supplier<?> defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * Assigns a value to this reference. This method can only be called once.
     *
     * @param value the value to set.
     * @throws AlreadyAssigned if this reference is already assigned a value.
     */
    @Override
    public void set(V value) {
        if (!ref.compareAndSet(UNASSIGNED, value)) {
            throw new AlreadyAssigned(ref.get(), value);
        }
    }

    /**
     * @return the referenced value. If the reference has not yet been set but is
     *         {@link #defaultTo(Supplier) initialized with a default value},
     *         the reference is assigned the default value and this is returned.
     */
    @Override
    @SuppressWarnings("unchecked")
    public V get() {
        Object maybeAssigned = ref.updateAndGet(v -> v != UNASSIGNED ? v : defaultValue.get());
        return maybeAssigned != UNASSIGNED ? (V) maybeAssigned : null;
    }



    public static final class AlreadyAssigned extends IllegalStateException {
        private static final long serialVersionUID = 1L;

        private AlreadyAssigned(Object alreadyAssignedValue, Object attemptedAssignedValue) {
            super("Already assigned to " + alreadyAssignedValue + ". Can not be reassigned to " + attemptedAssignedValue);
        }
    }

    private static final Object UNASSIGNED = new Object();

}
