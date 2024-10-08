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
import no.digipost.util.AutoClosed;
import no.digipost.util.ThrowingAutoClosed;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliterator;
import static java.util.stream.Collectors.joining;
import static java.util.stream.StreamSupport.stream;

/**
 * Party people, turn up tha...
 *
 * <p><strong>&ndash;&ndash; {@link DiggBase} &ndash;&ndash;</strong></p>
 *
 * <p>This class contains basic utilities. Basically.</p>
 */
public final class DiggBase {

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
        return nonNull(description, d -> t);
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
    public static <T, X extends Throwable> T nonNull(String description, T t, Function<? super String, X> throwIfNull) throws X {
        return nonNull(t, () -> throwIfNull.apply(description + " can not be null"));
    }

    /**
     * Not allow {@code null}-references.
     *
     * @param t           the reference
     * @param throwIfNull the exception to throw if {@code t} is {@code null}
     * @return            the same instance given as argument.
     * @throws X          if {@code t} is {@code null}.
     */
    public static <T, X extends Throwable> T nonNull(T t, Supplier<X> throwIfNull) throws X {
        return nonNull("a reference", s -> t, s -> throwIfNull.get());
    }

    /**
     * Not allow {@code null}-references. This is a convenience method for when a descriptive refKey
     * can be used to resolve the reference, for instance to resolve resources
     * on classpath with {@link Class#getResourceAsStream(String) .class::getResourceAsStream}.
     * The refKey will appear in the exception message if the resolved reference is {@code null}.
     *
     * @param descriptiveRefKey the key used to resolve the reference
     * @param refResolver       the function the will resolve the non-null result based on the description.
     * @return                  the reference resolved by {@code refResolver}, never {@code null}
     */
    public static <T> T nonNull(String descriptiveRefKey, Function<? super String, T> refResolver) {
        return nonNull(descriptiveRefKey, refResolver, d -> new NullPointerException("Tried to resolve " + d + ", but got null!"));
    }

    /**
     * Not allow {@code null}-references.
     *
     * @param descriptiveRefKey the key used to resolve the reference
     * @param refResolver       the function the will resolve the non-null result based on the description.
     * @param throwIfNull       the function to construct the exception if the {@code refResolver} yields {@code null}.
     * @return                  the reference resolved by {@code refResolver}, never {@code null}
     * @throws X                if {@code refResolver} yields {@code null}
     */
    public static <T, X extends Throwable> T nonNull(String descriptiveRefKey, Function<? super String, T> refResolver, Function<? super String, X> throwIfNull) throws X {
        T ref = refResolver.apply(descriptiveRefKey);
        if (ref != null) {
            return ref;
        } else {
            throw throwIfNull.apply(descriptiveRefKey);
        }
    }

    /**
     * The "friendly name" of a class is defined as its {@link Class#getSimpleName() simple name}, with
     * all enclosing classes prepended and joined with a <code>'.'</code> delimiter. This name is typically
     * useful for logging, naming based on classes, where the fully qualified name would be too verbose
     * and the simple name is not specific enough.
     * <p>
     * Given the following class model:
     * <pre>
     * class Base {
     *     class Nested {}
     * }
     * </pre>
     * The <em>friendly name</em> for the {@code Nested} class is "Base.Nested".
     *
     * @param clazz the clazz to get the friendly name of
     * @return the friendly name
     */
    public static String friendlyName(Class<?> clazz) {
        Deque<Class<?>> classes = new ArrayDeque<>();
        classes.add(clazz);
        for (Class<?> enclosing = clazz.getEnclosingClass(); enclosing != null; enclosing = enclosing.getEnclosingClass()) {
            classes.add(enclosing);
        }
        return stream(spliterator(classes.descendingIterator(), classes.size(), ORDERED), false)
            .map(Class::getSimpleName)
            .collect(joining("."));
    }

    /**
     * Extract (derive) multiple values from one given object.
     *
     * @param object the object to extract values from.
     * @param extractors each function that will extract a value from the given object. The resulting
     *                   value <em>must not</em> be {@code null}. Use {@link #extract(Object, Function...)}
     *                   if the extractors may yield non-existing values.
     *
     * @return a stream of the extracted values. The resulting stream will have the same size as the amount of
     *         given extractors.
     *
     * @param <T> The type of the object to extract from.
     * @param <R> The resulting most common type of the extracted values. Typically, the extractors
     *            should yield the same type.
     */
    @SafeVarargs
    @SuppressWarnings({"varargs"})
    public static final <T, R> Stream<R> extract(T object, Function<? super T, ? extends R> ... extractors) {
        return Stream.of(extractors).map(e -> e.apply(object));
    }

    /**
     * Extract (derive) multiple values from one given object. This will only include the "present"
     * values yielded from the given extractors, and is a shorthand for:
     * <p>
     * {@code {@link #extract(Object, Function...) extract(object, extractors...)}{@link Stream#filter(java.util.function.Predicate) .filter(}{@link Optional#isPresent() Optional::isPresent)}{@link Stream#map(Function) .map(}{@link Optional#get() Optional::get)}}
     * </p>
     *
     * @param object the object to extract values from.
     * @param extractors each function that will extract a value from the given object.
     *
     * @return a stream of values to be extracted. The resulting stream will have either the same size as
     *         or less than the amount of given extractors.
     *
     *
     * @param <T> The type of the object to extract from.
     * @param <R> The resulting most common type of the extracted values. Typically, the extractors
     *            should yield the same type.
     */
    @SafeVarargs
    @SuppressWarnings({"varargs"})
    public static final <T, R> Stream<R> extractIfPresent(T object, Function<? super T, ? extends Optional<R>> ... extractors) {
        return extract(object, extractors).filter(Optional::isPresent).map(Optional::get);
    }


    /**
     * Create a stream which will yield the exceptions from closing several {@link AutoCloseable closeables}.
     * Consuming the stream will ensure that <strong>all</strong> closeables are attempted
     * {@link AutoCloseable#close() closed}, and any exceptions happening will be available
     * through the returned stream.
     * <p>
     * To further collapse the possibly multiple exceptions into <em>one</em> throwable exception,
     * use either
     * {@link DiggCollectors#toSingleExceptionWithSuppressed() .collect(toSingleExceptionWithSuppressed())}
     * or {@link DiggCollectors#asSuppressedExceptionsOf(Throwable) .collect(asSuppressedExceptionsOf(..))}
     * in {@code DiggCollectors}.
     * <p>
     * If you have non-AutoCloseable related actions that need to be performed as well, this can be achieved
     * by using <code><a href="https://docs.oracle.com/javase/8/docs/api/java/util/stream/Stream.html#concat-java.util.stream.Stream-java.util.stream.Stream-">Stream.concat(</a>close(..),
     * {@link #forceOnAll(ThrowingConsumer, Object...) forceOnAll(T::action, T ... instances)})</code>
     *
     *
     * @param closeables The {@code AutoCloseable} instances to close.
     *
     * @return the Stream with exceptions, if any, from closing the closeables
     *
     * @see DiggCollectors#toSingleExceptionWithSuppressed()
     * @see DiggCollectors#asSuppressedExceptionsOf(Throwable)
     */
    public static Stream<Exception> close(AutoCloseable ... closeables) {
        return forceOnAll(AutoCloseable::close, closeables);
    }


    /**
     * Create a stream which will yield the exceptions (if any) from invoking an {@link ThrowingConsumer action} on
     * several {@code instances}. Consuming the returned stream will ensure that <strong>all</strong> instances will have
     * the action attempted on them, and any exceptions happening will be available through the returned stream.
     *
     * @param action the action to execute for each provided instance
     * @param instances the instances to act on with the provided {@code action}.
     *
     * @return the Stream with exceptions, if any
     */
    @SafeVarargs
    @SuppressWarnings({"varargs"})
    public static <T> Stream<Exception> forceOnAll(ThrowingConsumer<? super T, ? extends Exception> action, T ... instances) {
        return forceOnAll(action, Stream.of(instances));
    }


    /**
     * Create a stream which will yield the exceptions (if any) from invoking an {@link ThrowingConsumer action} on
     * several {@code instances}. This also includes exceptions thrown from <em>traversing</em> the given {@link Stream}
     * of instances, i.e. should resolving an element from the {@code Stream} cause an exception, it will be caught and
     * included in the returned {@code Stream}.
     * <p>
     * Consuming the returned stream will ensure that <strong>all</strong> traversed instances will have
     * the action attempted on them, and any exceptions happening will be available through the returned stream.
     *
     * @param action the action to execute for each provided instance
     * @param instances the instances to act on with the provided {@code action}.
     *
     * @return the Stream with exceptions, if any
     */
    public static <T> Stream<Exception> forceOnAll(ThrowingConsumer<? super T, ? extends Exception> action, Stream<T> instances) {
        return StreamSupport.stream(
                new FlatMapToExceptionSpliterator<>(action, instances.filter(Objects::nonNull).spliterator()),
                instances.isParallel());
    }

    private static final class FlatMapToExceptionSpliterator<W> implements Spliterator<Exception> {

        private final ThrowingConsumer<? super W, ? extends Exception> action;
        private final Spliterator<W> wrappedSpliterator;
        private final int characteristics;

        FlatMapToExceptionSpliterator(ThrowingConsumer<? super W, ? extends Exception> action, Spliterator<W> wrappedSpliterator) {
            this.action = action;
            this.wrappedSpliterator = wrappedSpliterator;
            this.characteristics = wrappedSpliterator.characteristics() & ~(SIZED | SUBSIZED | SORTED);
        }

        @Override
        public boolean tryAdvance(Consumer<? super Exception> exceptionConsumer) {
            try {
                return wrappedSpliterator.tryAdvance(action.ifException(exceptionConsumer::accept));
            } catch (Exception e) {
                exceptionConsumer.accept(e);
                return true;
            }
        }

        @Override
        public Spliterator<Exception> trySplit() {
            Spliterator<W> triedSplit = wrappedSpliterator.trySplit();
            return triedSplit != null ? new FlatMapToExceptionSpliterator<>(action, triedSplit) : null;
        }

        @Override
        public long estimateSize() {
            return Long.MAX_VALUE;
        }

        @Override
        public int characteristics() {
            return characteristics;
        }
    };


    /**
     * Wrap an arbitrary object to an {@link AutoCloseable} container, and assign an operation to be
     * performed on the wrapped object when calling {@link AutoCloseable#close()}. This can be
     * used for legacy classes which does not implement {@code AutoCloseable} to be used with the
     * {@code try-with-resources} construct. It should not be used (although it <em>can</em>) for
     * objects already implementing {@code AutoCloseable}.
     *
     * @param object the object to be managed with {@code try-with-resources}.
     * @param closeOperation the operation to invoke on {@code object} to close it.
     *
     * @return The wrapper which is {@link AutoCloseable}. Assign this with {@code try-with-resources} to
     *         have it properly closed.
     *
     *
     * @param <T> The type of the wrapped/managed object.
     * @param <X> The exception which may be throwed when closing by {@link AutoCloseable#close()}.
     *
     * @see #autoClose(Object, Consumer)
     */
    public static <T, X extends Exception> ThrowingAutoClosed<T, X> throwingAutoClose(T object, ThrowingConsumer<? super T, X> closeOperation) {
        return new ThrowingAutoClosed<T, X>(object, closeOperation);
    }


    /**
     * Wrap an arbitrary object to an {@link AutoCloseable} container, and assign an operation to be
     * performed on the wrapped object when calling {@link AutoCloseable#close()}. This can be
     * used for legacy classes which does not implement {@code AutoCloseable} to be used with the
     * {@code try-with-resources} construct. It should not be used (although it <em>can</em>) for
     * objects already implementing {@code AutoCloseable}.
     *
     * @param object the object to be managed with {@code try-with-resources}.
     * @param closeOperation the operation to invoke on {@code object} to close it. If the operation
     *                       can throw a checked exception, use {@link #throwingAutoClose(Object, ThrowingConsumer)}
     *                       instead.
     *
     * @return The wrapper which is {@link AutoCloseable}. Assign this with {@code try-with-resources} to
     *         have it properly closed.
     *
     *
     * @param <T> The type of the wrapped/managed object.
     *
     * @see #throwingAutoClose(Object, ThrowingConsumer)
     */
    public static <T> AutoClosed<T> autoClose(T object, Consumer<? super T> closeOperation) {
        return new AutoClosed<T>(object, closeOperation);
    }


    private DiggBase() {}

}
