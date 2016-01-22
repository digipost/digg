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

import no.digipost.DiggExceptions;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

@FunctionalInterface
public interface ThrowingFunction<T, R, X extends Throwable> {
    R apply(T t) throws X;


    default Function<T, R> asUnchecked() {
        return ifExceptionThrow(e -> DiggExceptions.asUnchecked(e));
    }

    default Function<T, R> ifExceptionThrow(Function<? super Exception, ? extends RuntimeException> exceptionMapper) {
        return ifExceptionThrow((t, e) -> exceptionMapper.apply(e));
    }

    default Function<T, R> ifExceptionThrow(BiFunction<? super T, ? super Exception, ? extends RuntimeException> exceptionMapper) {
        return ifException((t, e) -> { throw exceptionMapper.apply(t, e); }).andThen(Optional::get);
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
        return (t) -> after.apply(apply(t));
    }

}
