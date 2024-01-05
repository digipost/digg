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

import no.digipost.collection.ConflictingElementEncountered;
import no.digipost.collection.EnforceAtMostOneElementCollector;
import no.digipost.collection.EnforceDistinctFirstTupleElementCollector;
import no.digipost.collection.NonEmptyList;
import no.digipost.concurrent.OneTimeAssignment;
import no.digipost.stream.EmptyResultIfEmptySourceCollector;
import no.digipost.stream.NonEmptyStream;
import no.digipost.stream.SubjectFilter;
import no.digipost.tuple.Tuple;
import no.digipost.tuple.ViewableAsTuple;
import no.digipost.util.ViewableAsOptional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

/**
 * Various {@link java.util.stream.Collector} implementations.
 */
public final class DiggCollectors {


    /**
     * Create a collector used for finding and accumulating a specific result,
     * where applying a filter is not adequate. The returned
     * {@link SubjectFilter subject filter} is used for
     * further specifying the final (compound) condition for the result to find.
     * <p>
     * When searching for a result <em>in context</em> of other elements, you must carefully
     * ensure the source to have appropriate ordering and parallelity (or probably rather lack thereof)
     * for the correct and expected operation of the collector.
     * <p>
     * Note: because {@link Collector collectors} are applied to <em>all</em> elements
     * in a Stream, care should be taken to exclude non-applicable elements e.g. using
     * a {@link Stream#filter(Predicate) filter}, and {@link Stream#limit(long) limit}
     * especially for infinite Streams, before collecting.
     *
     *
     * @param <T> The element type which is inspected by the subject filter. This type is
     *            typically the same as the element type of the Stream the final collector
     *            is applied to.
     *
     * @param subjectElement the predicate for selecting a subject element for further use
     *                       in accumulating a result from applying the final collector.
     *
     * @return the subject filter for the collector, which must be further specified
     *         to build the final collector
     */
    public static <T> SubjectFilter<T> find(Predicate<T> subjectElement) {
        return new SubjectFilter<>(subjectElement);
    }


    /**
     * A <em>multituple</em> is similar to a <em>multimap</em> in that it consists of one {@link Tuple#first() first} value and a List of
     * values as the {@link Tuple#second() second} value,
     * and this collector will collect {@link ViewableAsTuple tuples} <strong><em>where it is expected that all the first tuple-elements
     * are equal</em></strong>, and re-arrange them by putting the distinct first element into a new Tuple, and collate each of the second
     * elements into a new List which is set as the second element of the new Tuple. If non-distinct values of the first elements of the
     * tuples are collected, a {@link ConflictingElementEncountered} is thrown.
     *
     * @param <T1> The type of the first tuple element, which will also become the type of the first element of the resulting {@code Tuple}.
     * @param <T2> The type of the second tuple element, which will become the {@code List<T2>} type of the second element of the
     *             resulting {@code Tuple}.
     *
     * @return the multituple collector.
     */
    public static <T1, T2> Collector<ViewableAsTuple<T1, Optional<T2>>, ?, Optional<Tuple<T1, List<T2>>>> toMultituple() {
        return toMultitupleOrThrowIfNonDistinct((alreadyCollected, conflicting) -> new ConflictingElementEncountered(alreadyCollected, conflicting,
                "the first element '" + conflicting.first() + "' of " + conflicting + " differs from the already collected first element '" + alreadyCollected.first() + "'"));
    }


    /**
     * A <em>multituple</em> is similar to a <em>multimap</em> in that it consists of one {@link Tuple#first() first} value and a List of
     * values as the {@link Tuple#second() second} value,
     * and this collector will collect {@link ViewableAsTuple tuples} <strong><em>where it is expected that all the first tuple-elements
     * are equal</em></strong>, and re-arrange them by putting the distinct first element into a new Tuple, and collate each of the second
     * elements into a new List which is set as the second element of the new Tuple. If non-distinct values of the first elements of the
     * tuples are collected, the exception returned from the given {@code exceptionOnNonDistinctFirstElement} function is thrown.
     *
     * @param <T1> The type of the first tuple element, which will also become the type of the first element of the resulting {@code Tuple}.
     * @param <T2> The type of the second tuple element, which will become the {@code List<T2>} type of the second element of the
     *             resulting {@code Tuple}.
     * @param exceptionOnNonDistinctFirstElement the function will be given the already collected multituple as its first argument and the
     *                                           unexpected conflicting tuple with non-distinct {@link Tuple#first() first} value as the second,
     *                                           which may be used to construct an exception to be thrown.
     *
     * @return the multituple collector.
     */
    public static <T1, T2> Collector<ViewableAsTuple<T1, Optional<T2>>, ?, Optional<Tuple<T1, List<T2>>>> toMultitupleOrThrowIfNonDistinct(
            BiFunction<? super Tuple<T1, List<T2>>, ? super Tuple<T1, Optional<T2>>, ? extends RuntimeException> exceptionOnNonDistinctFirstElement) {

        return new EnforceDistinctFirstTupleElementCollector<T1, T2>(exceptionOnNonDistinctFirstElement);
    }


    /**
     * A multimap maps from keys to lists, and this collector will arrange {@link ViewableAsTuple tuples}
     * by putting each distinct {@link Tuple#first() first tuple-element} as keys of the resulting map, mapping
     * them to a {@link List}, and adding each {@link Tuple#second() second tuple-element} to the list.
     *
     * @param <T1> The type of the first tuple element, which will become the key type of the resulting {@code Map}.
     * @param <T2> The type of the second tuple element, which will become the {@code List<T2>} value type of the
     *             resulting {@code Map}.
     *
     * @return the multimap collector.
     */
    public static <T1, T2> Collector<ViewableAsTuple<T1, Optional<T2>>, ?, Map<T1, List<T2>>> toMultimap() {
        Function<ViewableAsTuple<T1, Optional<T2>>, Tuple<T1, Optional<T2>>> asTuple = ViewableAsTuple::asTuple;
        return toMultimap(asTuple.andThen(Tuple::first), asTuple.andThen(Tuple::second));
    }

    public static <T, V> Collector<T, ?, Map<T, List<V>>> toMultimap(Function<? super T, Optional<V>> extractor) {
        return toMultimap(Function.identity(), extractor);
    }

    public static <T, K, V> Collector<T, ?, Map<K, List<V>>> toMultimap(Function<? super T, K> keyExtractor, Function<? super T, Optional<V>> extractor) {
        return Collectors.toMap(keyExtractor, extractor.andThen(DiggOptionals::toList), DiggCollectors::concat);
    }


    /**
     * This is a collector for accessing the <em>expected singular only</em> element of a {@link Stream}, as
     * it will throw an exception if more than one element is processed. This should be used in
     * preference of the {@link Stream#findFirst()} or {@link Stream#findAny()} when it is imperative
     * that the stream indeed yields a maximum of one single element, and any more elements is
     * considered a programming error.
     *
     * @return the collector
     */
    public static <T> EmptyResultIfEmptySourceCollector<T, OneTimeAssignment<T>, T> allowAtMostOne() {
        return allowAtMostOneOrElseThrow(ViewableAsOptional.TooManyElements::new);
    }


    /**
     * This is a collector for accessing the <em>expected singular only</em> element of a {@link Stream}, as
     * it will throw the exception yielded from the given function if more than one element is processed.
     *
     * @param exceptionOnExcessiveElements the function will be given the first element yielded from the stream
     *                                     as its first argument and the unexpected excess one as the second,
     *                                     which may be used to construct an exception to be thrown.
     * @return the collector
     * @see #allowAtMostOne()
     */
    public static <T> EmptyResultIfEmptySourceCollector<T, OneTimeAssignment<T>, T> allowAtMostOneOrElseThrow(BiFunction<? super T, ? super T, ? extends RuntimeException> exceptionOnExcessiveElements) {
        return new EnforceAtMostOneElementCollector<>(exceptionOnExcessiveElements);
    }


    /**
     * Add exceptions as {@link Throwable#addSuppressed(Throwable) suppressed} exception to a given
     * exception.
     *
     * @param exception The exception to add the suppressed exceptions to.
     * @return the collector
     */
    public static <X extends Throwable> Collector<Throwable, ?, X> asSuppressedExceptionsOf(X exception) {
        return collectingAndThen(toList(), suppressed -> {
            suppressed.forEach(exception::addSuppressed);
            return exception;
        });
    }


    /**
     * Collapse exceptions by taking the first (if any) and add every exception after the first
     * as {@link Throwable#addSuppressed(Throwable) suppressed} to the first one.
     *
     * @return the collector
     */
    public static <X extends Throwable> EmptyResultIfEmptySourceCollector<X, ?, X> toSingleExceptionWithSuppressed() {
        return EmptyResultIfEmptySourceCollector.from(collectingAndThen(toCollection(() -> new ConcurrentLinkedQueue<X>()),
                exceptions -> Optional.ofNullable(exceptions.poll()).map(firstException -> {
                    exceptions.forEach(firstException::addSuppressed);
                    return firstException;
                })));
    }


    /**
     * Collect element(s) to a {@link NonEmptyList}. If this collector is used with a
     * {@link NonEmptyStream}, the resulting list will be directly yielded by the
     * {@link NonEmptyStream#collect(EmptyResultIfEmptySourceCollector) collect} operation,
     * otherwise with regular {@link Stream streams}, the result will be an
     * {@link Optional Optional&lt;NonEmptyList&gt;}, which can be appropriately handled
     * in the event of a non-empty list being impossible to contruct because there
     * are no elements available to collect.
     *
     * @param <T> the type of elements contained in the resulting {@code NonEmptyList}
     * @return the collector
     */
    public static <T> EmptyResultIfEmptySourceCollector<T, ?, NonEmptyList<T>> toNonEmptyList() {
        return EmptyResultIfEmptySourceCollector.from(collectingAndThen(toList(), NonEmptyList::of));
    }



    private static <T> List<T> concat(Collection<? extends T> list1, Collection<? extends T> list2) {
        List<T> newList = new ArrayList<>(list1.size() + list2.size());
        newList.addAll(list1);
        newList.addAll(list2);
        return unmodifiableList(newList);
    }

    private DiggCollectors() {}

}
