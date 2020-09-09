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

import no.digipost.DiggExceptions;

import java.util.function.BiFunction;

@FunctionalInterface
public interface ThrowingBiFunction<T, U, R, X extends Throwable> {
    R apply(T t, U u) throws X;

    default BiFunction<T, U, R> asUnchecked() {
        return (t, u) -> {
            try {
                return ThrowingBiFunction.this.apply(t, u);
            } catch (RuntimeException | Error e) {
                throw e;
            } catch (Throwable e) {
                throw DiggExceptions.asUnchecked(e);
            }
        };
    }

    default <V> ThrowingBiFunction<T, U, V, X> andThen(ThrowingFunction<? super R, V, ? extends X> after) {
        return (t, u) -> after.apply(apply(t, u));
    }

}
