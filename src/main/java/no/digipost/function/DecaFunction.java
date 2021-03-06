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
package no.digipost.function;

import java.util.function.Function;

/**
 * Represents a function that accepts ten arguments and produces a result.
 * This is a ten-arity extension to the functional interfaces {@link Function}
 * and {@link java.util.function.BiFunction BiFunction} from the JDK.
 *
 * @param <T> the type of the first argument to the function
 * @param <U> the type of the second argument to the function
 * @param <V> the type of the third argument to the function
 * @param <W> the type of the fourth argument to the function
 * @param <X> the type of the fifth argument to the function
 * @param <Y> the type of the sixth argument to the function
 * @param <Z> the type of the seventh argument to the function
 * @param <A> the type of the eight argument to the function
 * @param <B> the type of the ninth argument to the function
 * @param <C> the type of the tenth argument to the function
 * @param <R> the type of the result of the function
 *
 * @see NonaFunction
 */
@FunctionalInterface
public interface DecaFunction<T, U, V, W, X, Y, Z, A, B, C, R> {

    R apply(T t, U u, V v, W w, X x, Y y, Z z, A a, B b, C c);

    default <S> DecaFunction<T, U, V, W, X, Y, Z, A, B, C, S> andThen(Function<? super R, S> after) {
        return (t, u, v, w, x, y, z, a, b, c) -> after.apply(apply(t, u, v, w, x, y, z, a, b, c));
    }

}

