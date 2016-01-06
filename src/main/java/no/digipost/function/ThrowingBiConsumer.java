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

import no.digipost.exceptions.Exceptions;

import java.util.function.BiConsumer;

@FunctionalInterface
public interface ThrowingBiConsumer<T, U, X extends Throwable> {
    void accept(T t, U u) throws X;

    default BiConsumer<T, U> asUnchecked() {
        return (t, u) -> {
            try {
                ThrowingBiConsumer.this.accept(t, u);
            } catch (RuntimeException | Error e) {
                throw e;
            } catch (Throwable e) {
                throw Exceptions.asUnchecked(e);
            }
        };
    }

    default ThrowingBiConsumer<T, U, X> andThen(ThrowingBiConsumer<? super T, ? super U, ? extends X> after) {
        return (t, u) -> {
            accept(t, u);
            after.accept(t, u);
        };
    }
}
