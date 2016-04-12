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

import java.util.Objects;
import java.util.function.Function;

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
     * Create a new tuple by applying a function to the first element, and putting the
     * result as the first element of the new tuple.
     *
     * @param mapper the function to apply to the first element
     * @return the new tuple
     */
    public <S1> Tuple<S1, T2> mapFirst(Function<? super T1, ? extends S1> mapper) {
        return map(mapper, Function.identity());
    }

    /**
     * @return the second value
     */
    public T2 second() {
        return second;
    }

    /**
     * Create a new tuple by applying a function to the second element, and putting the
     * result as the second element of the new tuple.
     *
     * @param mapper the function to apply to the second element
     * @return the new tuple
     */
    public <S2> Tuple<T1, S2> mapSecond(Function<? super T2, ? extends S2> mapper) {
        return map(Function.identity(), mapper);
    }


    /**
     * Create a new tuple by applying a function to each element, and putting the
     * results to corresponding positions in the new tuple.
     *
     * @param firstMapper the function to apply to the first element
     * @param secondMapper the function to apply to the second element
     * @return the new tuple
     */
    public <S1, S2> Tuple<S1, S2> map(Function<? super T1, ? extends S1> firstMapper, Function<? super T2, ? extends S2> secondMapper) {
        return Tuple.of(firstMapper.apply(first), secondMapper.apply(second));
    }



    /**
     * @return a new tuple with the same elements in swapped positions.
     */
    public Tuple<T2, T1> swap() {
        return Tuple.of(second, first);
    }

    /**
     * @return this tuple instance.
     */
    @Override
    public Tuple<T1, T2> asTuple() {
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Tuple) {
            Tuple<?, ?> that = (Tuple<?, ?>) obj;
            return Objects.equals(this.first, that.first) &&
                   Objects.equals(this.second, that.second);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }

    @Override
    public String toString() {
        return "[" + first + ", " + second + "]";
    }

}
