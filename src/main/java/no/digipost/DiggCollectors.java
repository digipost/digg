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

import no.digipost.tuple.Tuple;
import no.digipost.tuple.ViewableAsTuple;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static java.util.Collections.unmodifiableList;

public final class DiggCollectors {

    public static <T1, T2, TV extends ViewableAsTuple<T1, Optional<T2>>> Collector<TV, ?, Map<T1, List<T2>>> toMultimap() {
        Function<TV, Tuple<T1, Optional<T2>>> asTuple = ViewableAsTuple::asTuple;
        return toMultimap(asTuple.andThen(Tuple::first), asTuple.andThen(Tuple::second));
    }

    public static <T, V> Collector<T, ?, Map<T, List<V>>> toMultimap(Function<? super T, Optional<V>> extractor) {
        return toMultimap(Function.identity(), extractor);
    }

    public static <T, K, V> Collector<T, ?, Map<K, List<V>>> toMultimap(Function<? super T, K> keyExtractor, Function<? super T, Optional<V>> extractor) {
        return Collectors.toMap(keyExtractor, extractor.andThen(DiggOptionals::toList), DiggCollectors::concat);
    }


    private static <T> List<T> concat(Collection<? extends T> list1, Collection<? extends T> list2) {
        List<T> newList = new ArrayList<>(list1.size() + list2.size());
        newList.addAll(list1);
        newList.addAll(list2);
        return unmodifiableList(newList);
    }


    public DiggCollectors() {}
}
