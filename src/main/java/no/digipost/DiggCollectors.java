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

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static java.util.Collections.unmodifiableList;

public final class DiggCollectors {

    public static <T, A, R> AdaptableCollector<T, A, R> adapt(Collector<T, A, R> collector) {
        if (collector instanceof AdaptableCollector) {
            return (AdaptableCollector<T, A, R>) collector;
        } else {
            return AdaptableCollector.of(collector.supplier(), collector.accumulator(), collector.combiner(), collector.finisher(), collector.characteristics());
        }
    }

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


    private static <T> List<T> concat(Collection<? extends T> list1, Collection<? extends T> list2) {
        List<T> newList = new ArrayList<>(list1.size() + list2.size());
        newList.addAll(list1);
        newList.addAll(list2);
        return unmodifiableList(newList);
    }

    public static <T> AdaptableCollector<T, OneTimeAssignment<T>, Optional<T>> allowAtMostOne() {
        return adapt(new EnforceAtMostOneElementCollector<>());
    }

    public DiggCollectors() {}

}
