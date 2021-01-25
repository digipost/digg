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
package no.digipost.stream;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collector;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static no.digipost.DiggBase.friendlyName;

/**
 * A stream which is guarantied to produce at least one element. It provides
 * some extensions to the general {@link Stream} API for processing the stream knowing
 * it is non-empty, including, but not limited to:
 * <ul>
 *   <li>{@link #first()} and {@link #any()}</li>
 *   <li>{@link #limitToNonEmpty(long)}</li>
 *   <li>{@link #collect(EmptyIfEmptySourceCollector)}</li>
 *   <li>{@link #reduceFromFirst(BinaryOperator)}</li>
 * </ul>
 *
 * A selection of operations which maintains the cardinality of the stream returns {@code NonEmptyStream},
 * such as:
 * <ul>
 *   <li>{@link #map(Function)}
 * </ul>
 * This selection may be extended in the future. For operations which can not guarantie the
 * result will still be a non-empty stream, such as {@code filter} and {@code limit}, will yield a
 * regular {@link Stream}.
 *
 *
 * @param <T> the type of the stream elements
 */
public class NonEmptyStream<T> implements Stream<T> {

    public static <T> NonEmptyStream<T> of(T singleElement) {
        return of(singleElement, Stream.empty());
    }

    @SafeVarargs
    public static <T> NonEmptyStream<T> of(T firstElement, T ... remainingElements) {
        return of(firstElement, Arrays.stream(remainingElements));
    }

    public static <T> NonEmptyStream<T> of(T firstElement, Stream<T> remainingElements) {
        return of((Supplier<T>) () -> firstElement, remainingElements);
    }

    public static <T> NonEmptyStream<T> of(Supplier<? extends T> firstElement, Stream<T> remainingElements) {
        return new NonEmptyStream<>(firstElement, remainingElements);
    }


    private final Stream<T> completeStream;

    private NonEmptyStream(Supplier<? extends T> firstElement, Stream<T> remainingElements) {
        this(Stream.concat(Stream.generate(firstElement).limit(1), remainingElements));
    }


    /**
     * Constructor is only for <em>internal</em> use to wrap streams
     * already guarantied to be non-empty.
     */
    private NonEmptyStream(Stream<T> completeNonEmptyStream) {
        this.completeStream = completeNonEmptyStream;
    }

    @Override
    public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner) {
        return completeStream.collect(supplier, accumulator, combiner);
    }

    @Override
    public <R, A> R collect(Collector<? super T, A, R> collector) {
        return completeStream.collect(collector);
    }

    /**
     * Collect the stream elements by using a {@link EmptyIfEmptySourceCollector}.
     * <p>
     * This is an extension to the general {@link Stream} API, as a non-empty stream can always produce
     * a value without needing a provided initial "identity" value.
     *
     * @param collector the {@code Collector} describing the reduction
     * @return the result from collecting the elements
     *
     * @see EmptyIfEmptySourceCollector
     */
    public <A, R> R collect(EmptyIfEmptySourceCollector<? super T, A, R> collector) {
        @SuppressWarnings("unchecked")
        Collector<Object, A, Optional<R>> generalCollector = Collector.class.cast(collector);
        return collect(generalCollector)
                .orElseThrow(() -> new IllegalStateException("Unexpected empty stream using Collector of type " + friendlyName(collector.getClass())));
    }

    @Override
    public <R> NonEmptyStream<R> map(Function<? super T, ? extends R> mapper) {
        return new NonEmptyStream<>(completeStream.map(mapper));
    }

    @Override
    public Iterator<T> iterator() {
        return completeStream.iterator();
    }

    @Override
    public Spliterator<T> spliterator() {
        return completeStream.spliterator();
    }

    @Override
    public boolean isParallel() {
        return completeStream.isParallel();
    }

    @Override
    public NonEmptyStream<T> sequential() {
        return isParallel() ? new NonEmptyStream<>(completeStream.sequential()) : this;
    }

    @Override
    public NonEmptyStream<T> parallel() {
        return isParallel() ? this : new NonEmptyStream<>(completeStream.parallel());
    }

    @Override
    public NonEmptyStream<T> unordered() {
        return new NonEmptyStream<>(completeStream.unordered());
    }

    @Override
    public NonEmptyStream<T> onClose(Runnable closeHandler) {
        return new NonEmptyStream<>(completeStream.onClose(closeHandler));
    }

    @Override
    public void close() {
        completeStream.close();
    }

    @Override
    public Stream<T> filter(Predicate<? super T> predicate) {
        return completeStream.filter(predicate);
    }

    @Override
    public IntStream mapToInt(ToIntFunction<? super T> mapper) {
        return completeStream.mapToInt(mapper);
    }

    @Override
    public LongStream mapToLong(ToLongFunction<? super T> mapper) {
        return completeStream.mapToLong(mapper);
    }

    @Override
    public DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper) {
        return completeStream.mapToDouble(mapper);
    }

    @Override
    public <R> Stream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
        return completeStream.flatMap(mapper);
    }

    @Override
    public IntStream flatMapToInt(Function<? super T, ? extends IntStream> mapper) {
        return completeStream.flatMapToInt(mapper);
    }

    @Override
    public LongStream flatMapToLong(Function<? super T, ? extends LongStream> mapper) {
        return completeStream.flatMapToLong(mapper);
    }

    @Override
    public DoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> mapper) {
        return completeStream.flatMapToDouble(mapper);
    }

    @Override
    public NonEmptyStream<T> distinct() {
        return new NonEmptyStream<>(completeStream.distinct());
    }

    @Override
    public NonEmptyStream<T> sorted() {
        return new NonEmptyStream<>(completeStream.sorted());
    }

    @Override
    public NonEmptyStream<T> sorted(Comparator<? super T> comparator) {
        return new NonEmptyStream<>(completeStream.sorted(comparator));
    }

    @Override
    public NonEmptyStream<T> peek(Consumer<? super T> action) {
        return new NonEmptyStream<>(completeStream.peek(action));
    }

    /**
     * Returns a stream consisting of the elements of this stream, truncated to be
     * no longer than {@code maxSizeMoreThanZero} in length.
     *
     * @param maxSizeMoreThanZero the number of elements the stream should be limited to,
     *                            must be 1 or greater ({@code >= 1})
     * @return the new non-empty stream
     *
     * @throws IllegalArgumentException if the given {@code maxSizeMoreThanZero} is 0 or less
     *
     * @see #limit(long)
     */
    public NonEmptyStream<T> limitToNonEmpty(long maxSizeMoreThanZero) {
        if (maxSizeMoreThanZero < 1) {
            throw new IllegalArgumentException("Can not limit to " + maxSizeMoreThanZero + " and still be a non-empty stream");
        }
        return new NonEmptyStream<>(limit(maxSizeMoreThanZero));
    }

    @Override
    public Stream<T> limit(long maxSize) {
        return completeStream.limit(maxSize);
    }

    @Override
    public Stream<T> skip(long n) {
        return completeStream.skip(n);
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        completeStream.forEach(action);
    }

    @Override
    public void forEachOrdered(Consumer<? super T> action) {
        completeStream.forEachOrdered(action);
    }

    @Override
    public Object[] toArray() {
        return completeStream.toArray();
    }

    @Override
    public <A> A[] toArray(IntFunction<A[]> generator) {
        return completeStream.toArray(generator);
    }

    @Override
    public T reduce(T identity, BinaryOperator<T> accumulator) {
        return completeStream.reduce(identity, accumulator);
    }

    /**
     * Performs a reduction on the elements of this stream, using the first element of the stream
     * (guarantied to be available) and an associative accumulation function, and returns the reduced value.
     * <p>
     * This is an extension to the general {@link Stream} API, as a non-empty stream can always produce
     * a value without needing a provided initial "identity" value.
     *
     * @param accumulator an associative, non-interfering, stateless function for combining two values
     * @return the result of the reduction
     *
     * @see #reduce(Object, BinaryOperator)
     */
    public T reduceFromFirst(BinaryOperator<T> accumulator) {
        return reduce(accumulator).orElseThrow(() -> new IllegalStateException("Unexpected empty stream"));
    }

    @Override
    public Optional<T> reduce(BinaryOperator<T> accumulator) {
        return completeStream.reduce(accumulator);
    }

    @Override
    public <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner) {
        return completeStream.reduce(identity, accumulator, combiner);
    }

    @Override
    public Optional<T> min(Comparator<? super T> comparator) {
        return completeStream.min(comparator);
    }

    @Override
    public Optional<T> max(Comparator<? super T> comparator) {
        return completeStream.max(comparator);
    }

    @Override
    public long count() {
        return completeStream.count();
    }

    @Override
    public boolean anyMatch(Predicate<? super T> predicate) {
        return completeStream.anyMatch(predicate);
    }

    @Override
    public boolean allMatch(Predicate<? super T> predicate) {
        return completeStream.allMatch(predicate);
    }

    @Override
    public boolean noneMatch(Predicate<? super T> predicate) {
        return completeStream.noneMatch(predicate);
    }

    /**
     * Return the first element of the stream.
     * <p>
     * This is an extension to the general {@link Stream} API, as a non-empty stream
     * is guarantied to always have at least one value.
     *
     * @return the first value of the stream
     *
     * @see #findFirst()
     */
    public T first() {
        return findFirst().orElseThrow(() -> new IllegalStateException("Unexpected empty stream"));
    }

    /**
     * {@inheritDoc}
     * <p>
     * <strong>As this is a non-empty stream, this method always returns a
     * non-empty {@link Optional} with the first value of the stream.</strong>
     *
     * @return an Optional containing the first element of the stream
     *
     * @see #first()
     */
    @Override
    public Optional<T> findFirst() {
        return completeStream.findFirst();
    }

    /**
     * Return any element from the stream.
     * <p>
     * This is an extension to the general {@link Stream} API, as a non-empty stream
     * is guarantied to always have at least one value.
     *
     * @return any value from the stream
     *
     * @see #findAny()
     */
    public T any() {
        return findAny().orElseThrow(() -> new IllegalStateException("Unexpected empty stream"));
    }


    /**
     * {@inheritDoc}
     * <p>
     * <strong>As this is a non-empty stream, this method always returns a
     * non-empty {@link Optional} with the any value from the stream.</strong>
     *
     * @return an Optional containing any element from the stream
     *
     * @see #any()
     */
    @Override
    public Optional<T> findAny() {
        return completeStream.findAny();
    }
}
