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
package no.digipost.function;

import java.util.function.Function;

/**
 * Represents a function that accepts five arguments and produces a result.
 * This is a five-arity extension to the functional interfaces {@link Function}
 * and {@link java.util.function.BiFunction BiFunction} from the JDK.
 *
 * @param <T> the type of the first argument to the function
 * @param <U> the type of the second argument to the function
 * @param <V> the type of the third argument to the function
 * @param <W> the type of the fourth argument to the function
 * @param <X> the type of the fifth argument to the function
 * @param <R> the type of the result of the function
 *
 * @see QuadFunction
 */
@FunctionalInterface
public interface PentaFunction<T, U, V, W, X, R> {

    R apply(T t, U u, V v, W w, X x);

    default <S> PentaFunction<T, U, V, W, X, S> andThen(Function<? super R, S> after) {
        return (t, u, v, w, x) -> after.apply(apply(t, u, v, w, x));
    }

}

