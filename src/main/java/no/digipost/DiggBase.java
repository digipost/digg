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

import no.digipost.function.ThrowingConsumer;
import no.digipost.util.AutoClosed;
import no.digipost.util.ThrowingAutoClosed;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliterator;
import static java.util.stream.Collectors.joining;
import static java.util.stream.StreamSupport.stream;

/**
 * Party people, turn up tha...
 *
 * <h1>&ndash;&ndash; {@link DiggBase} &ndash;&ndash;</h1>
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
     * all enclosing classes prepended and joined with a <tt>'.'</tt> delimiter. This name is typically
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
    public static final <T, R> Stream<R> extractIfPresent(T object, Function<? super T, ? extends Optional<R>> ... extractors) {
        return extract(object, extractors).filter(Optional::isPresent).map(Optional::get);
    }


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
