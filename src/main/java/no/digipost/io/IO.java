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
package no.digipost.io;

import no.digipost.DiggIO;
import no.digipost.function.ThrowingConsumer;
import no.digipost.function.ThrowingFunction;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @deprecated use {@link DiggIO} instead.
 */
@Deprecated
public final class IO {

    /**
     * @deprecated use {@link DiggIO#autoClosing(ThrowingConsumer)} instead.
     */
    @Deprecated
    public static <T extends AutoCloseable> Consumer<T> autoClosing(ThrowingConsumer<T, ? extends Exception> consumer) {
        return DiggIO.autoClosing(consumer);
    }

    /**
     * @deprecated use {@link DiggIO#autoClosing(ThrowingFunction)} instead.
     */
    @Deprecated
    public static <T extends AutoCloseable, R> Function<T, R> autoClosing(ThrowingFunction<T, R, ? extends Exception> function) {
        return DiggIO.autoClosing(function);
    }

    private IO() {}
}
