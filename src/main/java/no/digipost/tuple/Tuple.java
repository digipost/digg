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
package no.digipost.tuple;

/**
 * A tuple is a simple composition of two arbitrary values (objects). A tuple
 * captures no semantics of the two values, and they are only referred to as
 * "the first" and "the second" value.
 *
 * @see ViewableAsTuple
 *
 * @param <T1> The type of the first value
 * @param <T2> The type of the second value
 */
public final class Tuple<T1, T2> implements ViewableAsTuple<T1, T2> {

    public static final <T1, T2> Tuple<T1, T2> of(T1 first, T2 second) {
        return new Tuple<>(first, second);
    }

    private final T1 first;
    private final T2 second;

    private Tuple(T1 first, T2 second) {
        this.first = first;
        this.second = second;
    }

    /**
     * @return the first value
     */
    public T1 first() {
        return first;
    }

    /**
     * @return the second value
     */
    public T2 second() {
        return second;
    }

    /**
     * @return this tuple instance.
     */
    @Override
    public Tuple<T1, T2> asTuple() {
        return this;
    }

}
