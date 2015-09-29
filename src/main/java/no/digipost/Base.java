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
package no.digipost;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Party people, turn up tha...
 *
 * <h1>&ndash;&ndash; {@link Base} &ndash;&ndash;</h1>
 *
 * <p>This class contains basic utilities. Basically.</p>
 */
public final class Base {

    /**
     * Not allow {@code null}-references.
     *
     * @param description A small description of the reference. Will be used in the exception message if
     *                    the reference is {@code null}
     * @param t           the reference
     * @return            the same instance given as argument.
     * @throws NullPointerException if {@code t} is {@code null}.
     */
    public static <T> T nonNull(String description, T t) {
        return nonNull(description, t, NullPointerException::new);
    }

    /**
     * Not allow {@code null}-references.
     *
     * @param description A small description of the reference. Will be used in the exception message if
     *                    the reference is {@code null}
     * @param t           the reference
     * @param throwIfNull Construct the exception to be thrown if the reference is {@code null}.
     * @return            the same instance given as argument.
     * @throws X          if {@code t} is {@code null}.
     */
    public static <T, X extends Throwable> T nonNull(String description, T t, Function<String, X> throwIfNull) throws X {
        return nonNull(t, () -> throwIfNull.apply(description + " can not be null"));
    }

    /**
     * Not allow {@code null}-references.
     *
     * @param t the reference
     * @param throwIfNull the exception to throw if {@code t} is {@code null}
     * @return the same instance given as argument.
     * @throws X if {@code t} is {@code null}.
     */
    public static <T, X extends Throwable> T nonNull(T t, Supplier<X> throwIfNull) throws X {
        if (t != null) {
            return t;
        } else {
            throw throwIfNull.get();
        }
    }


    /**
     * Extract (derive) multiple values from one given object.
     *
     * @param object the object to extract values from.
     * @param extractors each function that will extract a value from the given object.
     *
     * @return a stream of values to be extracted.
     *
     *
     * @param <T> The type of the object to extract from.
     * @param <R> The resulting most common type of the extracted values. Typically, the extractors
     *            should yield the same type.
     */
    @SafeVarargs
    public static final <T, R> Stream<R> extract(T object, Function<? super T, ? extends Optional<R>> ... extractors) {
        return Stream.of(extractors).map(f -> f.apply(object)).filter(Optional::isPresent).map(Optional::get);
    }

    private Base() {}

}
