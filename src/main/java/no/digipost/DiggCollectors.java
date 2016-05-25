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

import no.digipost.collection.AdaptableCollector;
import no.digipost.collection.EnforceAtMostOneElementCollector;
import no.digipost.concurrent.OneTimeAssignment;
import no.digipost.tuple.Tuple;
import no.digipost.tuple.ViewableAsTuple;
import no.digipost.util.ViewableAsOptional;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableList;

/**
 * Various {@link java.util.stream.Collector} implementations.
 */
public final class DiggCollectors {

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
    public static <T1, T2> AdaptableCollector<ViewableAsTuple<T1, Optional<T2>>, ?, Map<T1, List<T2>>> toMultimap() {
        Function<ViewableAsTuple<T1, Optional<T2>>, Tuple<T1, Optional<T2>>> asTuple = ViewableAsTuple::asTuple;
        return toMultimap(asTuple.andThen(Tuple::first), asTuple.andThen(Tuple::second));
    }

    public static <T, V> AdaptableCollector<T, ?, Map<T, List<V>>> toMultimap(Function<? super T, Optional<V>> extractor) {
        return toMultimap(Function.identity(), extractor);
    }

    public static <T, K, V> AdaptableCollector<T, ?, Map<K, List<V>>> toMultimap(Function<? super T, K> keyExtractor, Function<? super T, Optional<V>> extractor) {
        return adapt(Collectors.toMap(keyExtractor, extractor.andThen(DiggOptionals::toList), DiggCollectors::concat));
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
    public static <T> AdaptableCollector<T, OneTimeAssignment<T>, Optional<T>> allowAtMostOne() {
        return allowAtMostOneOrElseThrow(ViewableAsOptional.TooManyElements::new);
    }

    /**
     * This is a collector for accessing the <em>expected singular only</em> element of a {@link Stream}, as
     * it will throw the exception yielded from the given function if more than one element is processed.
     *
     * @param the function will be given the first element yielded from the stream as its first argument
     *                     and the unexpected excess one as the second, which may be used to construct an
     *                     exception to be thrown.
     * @return the collector
     * @see #allowAtMostOne()
     */
    public static <T> AdaptableCollector<T, OneTimeAssignment<T>, Optional<T>> allowAtMostOneOrElseThrow(BiFunction<? super T, ? super T, ? extends RuntimeException> exceptionOnExcessiveElements) {
        return adapt(new EnforceAtMostOneElementCollector<>(exceptionOnExcessiveElements));
    }


    /**
     * Turn any {@link Collector} into an {@link AdaptableCollector}.
     *
     * @param collector the collector to convert
     *
     * @return the {@link AdaptableCollector}
     *
     * @see AdaptableCollector
     */
    public static <T, A, R> AdaptableCollector<T, A, R> adapt(Collector<T, A, R> collector) {
        if (collector instanceof AdaptableCollector) {
            return (AdaptableCollector<T, A, R>) collector;
        } else {
            return AdaptableCollector.of(collector.supplier(), collector.accumulator(), collector.combiner(), collector.finisher(), collector.characteristics());
        }
    }



    private static <T> List<T> concat(Collection<? extends T> list1, Collection<? extends T> list2) {
        List<T> newList = new ArrayList<>(list1.size() + list2.size());
        newList.addAll(list1);
        newList.addAll(list2);
        return unmodifiableList(newList);
    }

    private DiggCollectors() {}

}
