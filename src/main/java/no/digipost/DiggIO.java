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
package no.digipost;

import no.digipost.function.ThrowingConsumer;
import no.digipost.function.ThrowingFunction;
import no.digipost.io.DataSize;
import no.digipost.io.LimitedInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static no.digipost.DiggExceptions.asUnchecked;

/**
 * IO-related utilities.
 */
public final class DiggIO {

    /**
     * Wrap a consumer which processes an {@link AutoCloseable}
     * (typically an InputStream or similar) into a new {@code Consumer} which will always close
     * the {@code AutoCloseable} when the given consumer returns, successfully or throwing an exception.
     *
     * @param consumer the {@link ThrowingConsumer} taking an {@link AutoCloseable} as argument.
     * @return the {@link Consumer} which will handle closing of the passed {@code AutoCloseable}.
     */
    public static <T extends AutoCloseable> Consumer<T> autoClosing(ThrowingConsumer<T, ? extends Exception> consumer) {
        return t1 ->
            autoClosing((T t2) -> {
                consumer.accept(t2);
                return null;
            }).apply(t1);
    }


    /**
     * Wrap a function which yields a result from processing an {@link AutoCloseable}
     * (typically an InputStream or similar) into a new {@code Function} which will always close
     * the {@code AutoCloseable} when the given function returns, successfully or throwing an exception.
     *
     * @param function the {@link ThrowingFunction} taking an {@link AutoCloseable} as argument.
     * @return the {@link Function} which will handle closing of the passed {@code AutoCloseable}.
     */
    public static <T extends AutoCloseable, R> Function<T, R> autoClosing(ThrowingFunction<T, R, ? extends Exception> function) {
        return closeable -> {
            try (T managed = closeable) {
                return function.apply(managed);
            } catch (Exception e) {
                throw asUnchecked(e);
            }
        };
    }

    /**
     * Limit the number of bytes to be read from an {@link InputStream}. The returned stream silently ends reading, i.e. returns
     * {@code -1} from {@link InputStream#read() .read()}, if the read bytes exceeds the given threshold. This behavior is appropriate
     * when it is actually <em>not desireable to consume the entire stream</em>, typically for processing some data known to be located in the leading
     * part of the stream, and it is imperative to protect against accidentally consuming an entire (potentially large) stream.
     *
     * @param inputStream The {@code InputStream} to limit bytes to read from.
     * @param maxDataToRead The limit of data to read.
     *
     * @see #limit(InputStream, DataSize, Supplier)
     */
     public static InputStream limit(InputStream inputStream, DataSize maxDataToRead) {
         return new LimitedInputStream(inputStream, maxDataToRead.toBytes(), LimitedInputStream.SILENTLY_EOF_ON_REACHING_LIMIT);
     }


   /**
    * Limit the number of bytes to be read from an {@link InputStream}. If the number of bytes exceeds the given threshold, an exception
    * will be thrown.
    *
    * @param inputStream The {@code InputStream} to limit bytes to read from.
    * @param maxDataToRead The limit of data to read.
    * @param throwIfTooManyBytes Supplier of exception to throw if more bytes are read than the max allowed. If the supplier returns
    *                            a non-{@link RuntimeException} which is <em>not</em> an {@link IOException}, it will be wrapped in a {@code RuntimeException}.
    */
    public static InputStream limit(InputStream inputStream, DataSize maxDataToRead, Supplier<? extends Exception> throwIfTooManyBytes) {
        return new LimitedInputStream(inputStream, maxDataToRead.toBytes(), throwIfTooManyBytes);
    }



    private DiggIO() {}
}
