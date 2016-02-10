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
package no.digipost.util;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * An assignment may or may not be assigned, the assigned value can be retrieved with {@link #get()}
 * and will return {@code null} if unassigned, and may be assigned with {@link #set(Object)} if the
 * assignment allows (re-)assignment.
 *
 * @param <V> The type that may be assigned (or not).
 */
public interface Assignment<V> extends Supplier<V>, Consumer<V>, ViewableAsOptional.Single<V> {

    /**
     * Create a new {@link Assignment} which uses an {@link AtomicReference}
     * to hold the assigned value.
     *
     * @param reference the holder of the assigned value
     *
     * @return the new {@code Assignment} using the given {@code AtomicReference}.
     */
    static <V> Assignment<V> from(AtomicReference<V> reference) {
        return from(reference, AtomicReference::get, AtomicReference::set);
    }


    /**
     * Create a new {@link Assignment}, which assigns to and retrieves from an arbitrary container object.
     *
     * @param container The holder of the assigned value.
     * @param getter how the get the value from the container
     * @param setter how to set the value on the container
     *
     * @return the get and set operations as a new {@code Assignment}.
     */
    static <V, S> Assignment<V> from(S container, Function<? super S, V> getter, BiConsumer<? super S, ? super V> setter) {
        return new Assignment<V>() {
            @Override
            public V get() {
                return getter.apply(container);
            }
            @Override
            public void set(V value) {
                setter.accept(container, value);
            }
        };
    }

    /**
     * Assign the given value.
     *
     * @param value the value to assign.
     */
    void set(V value);

    @Override
    default void accept(V value) {
        set(value);
    }

    /**
     * Create a {@link ChainableAssignment} from this assignment.
     *
     * @see ChainableAssignment
     */
    default <C> ChainableAssignment<V, C> chainableWith(C chainReturnObject) {
        return new ChainableAssignment<>(this, chainReturnObject);
    }
}
