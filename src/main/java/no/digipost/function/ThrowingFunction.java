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

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

@FunctionalInterface
public interface ThrowingFunction<T, R, X extends Throwable> {

    /**
     * Returns a function that always returns its input argument.
     *
     * @param <T> the type of the input and output objects to the function
     * @return a function that always returns its input argument
     */
    static <T> ThrowingFunction<T, T, RuntimeException> identity() {
        return t -> t;
    }


    R apply(T t) throws X;


    default Function<T, R> asUnchecked() {
        return ifExceptionThrow(e -> DiggExceptions.asUnchecked(e));
    }

    default Function<T, R> ifExceptionThrow(Function<? super Exception, ? extends RuntimeException> exceptionMapper) {
        return ifExceptionThrow((t, e) -> exceptionMapper.apply(e));
    }

    default Function<T, R> ifExceptionThrow(BiFunction<? super T, ? super Exception, ? extends RuntimeException> exceptionMapper) {
        return ifException((t, e) -> { throw exceptionMapper.apply(t, e); }).andThen(o -> o.orElse(null));
    }

    default Function<T, Optional<R>> ifException(Consumer<Exception> exceptionHandler) {
        return ifException((t, e) -> exceptionHandler.accept(e));
    }

    default Function<T, Optional<R>> ifException(BiConsumer<? super T, Exception> exceptionHandler) {
        return t -> {
            return Optional.ofNullable(ifExceptionApply((failingT, e) -> {
                exceptionHandler.accept(failingT, e);
                return null;
            }).apply(t));
        };
    }

    default Function<T, R> ifExceptionApply(Function<Exception, ? extends R> exceptionMapper) {
        return ifExceptionApply((t, e) -> exceptionMapper.apply(e));
    }

    default Function<T, R> ifExceptionApply(BiFunction<? super T, Exception, ? extends R> exceptionMapper) {
        return t -> {
            try {
                return apply(t);
            } catch (Exception e) {
                return exceptionMapper.apply(t, e);
            } catch (Error err) {
                throw err;
            } catch (Throwable thr) {
                throw DiggExceptions.asUnchecked(thr);
            }
        };
    }

    default <V> ThrowingFunction<T, V, X> andThen(ThrowingFunction<? super R, V, ? extends X> after) {
        return t -> after.apply(apply(t));
    }

    default <V> ThrowingFunction<V, R, X> compose(ThrowingFunction<? super V, ? extends T, ? extends X> before) {
        return t -> apply(before.apply(t));
    }

}
