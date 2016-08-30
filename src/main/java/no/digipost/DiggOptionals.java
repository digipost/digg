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

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableList;
import static java.util.Spliterator.IMMUTABLE;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliterator;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.StreamSupport.stream;

/**
 * Utilities for working with {@link Optional}s.
 */
public final class DiggOptionals {

    /**
     * Convert {@link Supplier suppliers} of {@link Optional} to a {@link Stream} consisting of only the
     * {@link Optional#isPresent() present} optionals. The returned stream is {@link Stream#sequential()}, yielding
     * the present values in the order they are given. Depending on how the stream is consumed, only the necessary
     * optional values will be attempted resolved. E.g. calling {@link Stream#findFirst() .findFirst()} will only resolve
     * as many {@code Optional}s needed until the first present one is encountered.
     *
     * <h2>Note on parallel streams</h2>
     * The returned stream will be sequential and appropriate for use to resolve several {@link Optional}s in a deterministic
     * order, or for instance to find the first present value of a prioritized set of possible ways to resolve it. One should
     * be <em>very</em> (as always with streams) careful with {@link Stream#parallel() parallelizing} the stream, as you generally
     * have no control with how the resolvers will be run. For instance, {@link Stream#findAny() .parallel().findAny()} will indeed return
     * the the first resolved value, <em>but still block until the workers already started by the stream has terminated</em>.
     *
     * @param <T> The type of the given {@code Optional}s and returned {@code Stream}.
     * @param optionalResolvers The operations resolving {@code Optional}s to convert to a {@code Stream}.
     *
     * @return a {@code Stream} containing the present {@code Optional}s
     */
    @SafeVarargs
    public static <T> Stream<T> toStream(Supplier<Optional<T>> ... optionalResolvers) {
        Supplier<Optional<T>>[] copiedResolvers = Arrays.copyOf(optionalResolvers, optionalResolvers.length);
        Spliterator<Supplier<Optional<T>>> spliterator = spliterator(copiedResolvers, IMMUTABLE | ORDERED);
        return stream(spliterator, false).map(Supplier::get).filter(Optional::isPresent).map(Optional::get);
    }


    /**
     * Convert {@link Optional}s to a {@link Stream} consisting of only the
     * {@link Optional#isPresent() present} optionals.
     *
     * @param <T> The type of the given {@code Optional}s and returned {@code Stream}.
     * @param optionals The {@code Optional}s to convert to a {@code Stream}.
     *
     * @return a {@code Stream} containing the present {@code Optional}s
     */
    @SafeVarargs
    public static <T> Stream<T> toStream(Optional<? extends T> ... optionals) {
        return Stream.of(optionals).filter(Optional::isPresent).map(Optional::get);
    }

    /**
     * Unwrap an {@link Optional} into a zero or one element {@link Collections#unmodifiableList(List) unmodifiable} list.
     *
     * @param <T> The type of the optional value.
     * @param optional The optional value to be wrapped in a list.
     *
     * @return list containing either exactly one or zero elements depending of the
     *         {@code Optional} being present or not.
     */
    public static <T> List<T> toList(Optional<T> optional) {
        return unmodifiableList(collect(optional, toCollection(() -> new ArrayList<>(1))));
    }

    /**
     * Unwrap an {@link Optional} into another container using a {@link Collector}.
     *
     * @param <T> The type of the optional value.
     * @param <R> The resulting container type.
     *
     * @param optional The optional value to be wrapped in another container.
     * @param collector The collector to use for wrapping into another container.
     *
     * @return The new container with either exactly one or zero elements depending of the
     *         {@code Optional} being present or not.
     */
    public static <T, A, R> R collect(Optional<T> optional, Collector<T, A, R> collector) {
        A container = collector.supplier().get();
        optional.ifPresent(o -> collector.accumulator().accept(container, o));
        return collector.finisher().apply(container);
    }


    public DiggOptionals() {}
}
