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

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static no.digipost.DiggExceptions.rethrowAnyException;

@FunctionalInterface
public interface ThrowingConsumer<T, X extends Throwable> {
    void accept(T t) throws X;

    default Consumer<T> asUnchecked() {
        return ifException(rethrowAnyException);
    }

    default Consumer<T> ifException(Consumer<Exception> exceptionHandler) {
        return ifException((t, e) -> exceptionHandler.accept(e));
    }

    default Consumer<T> ifException(BiConsumer<? super T, Exception> exceptionHandler) {
        return t -> {
            try {
                accept(t);
            } catch (Exception e) {
                exceptionHandler.accept(t, e);
            } catch (Error err) {
                throw err;
            } catch (Throwable thr) {
                throw DiggExceptions.asUnchecked(thr);
            }
        };
    }

    default ThrowingConsumer<T, X> andThen(ThrowingConsumer<? super T, ? extends X> after) {
        return t -> {
            accept(t);
            after.accept(t);
        };
    }
}
