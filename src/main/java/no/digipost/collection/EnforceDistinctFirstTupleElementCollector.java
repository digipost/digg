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
package no.digipost.collection;

import no.digipost.concurrent.OneTimeAssignment;
import no.digipost.concurrent.OneTimeAssignment.AlreadyAssigned;
import no.digipost.tuple.Tuple;
import no.digipost.tuple.ViewableAsTuple;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.*;
import java.util.stream.Collector;

import static java.util.Collections.unmodifiableSet;


public class EnforceDistinctFirstTupleElementCollector<T1, T2> implements Collector<ViewableAsTuple<T1, Optional<T2>>, MultitupleBuilder<T1, T2>, Optional<Tuple<T1, List<T2>>>> {

    private static final Set<Characteristics> CHARACTERISTICS = unmodifiableSet(EnumSet.noneOf(Characteristics.class));

    private final BiFunction<? super Tuple<T1, List<T2>>, ? super Tuple<T1, Optional<T2>>, ? extends RuntimeException> exceptionOnNonDistinctFirstElement;

    public EnforceDistinctFirstTupleElementCollector(BiFunction<? super Tuple<T1, List<T2>>, ? super Tuple<T1, Optional<T2>>, ? extends RuntimeException> exceptionOnNonDistinctFirstElement) {
        this.exceptionOnNonDistinctFirstElement = exceptionOnNonDistinctFirstElement;

    }

    @Override
    public Supplier<MultitupleBuilder<T1, T2>> supplier() {
        return () -> new MultitupleBuilder<>(exceptionOnNonDistinctFirstElement);
    }

    @Override
    public BiConsumer<MultitupleBuilder<T1, T2>, ViewableAsTuple<T1, Optional<T2>>> accumulator() {
        return MultitupleBuilder::tryAccumulate;
    }

    @Override
    public BinaryOperator<MultitupleBuilder<T1, T2>> combiner() {
        return (m1, m2) -> { throw new UnsupportedOperationException("combiner() is not supported"); } ;
    }

    @Override
    public Function<MultitupleBuilder<T1, T2>, Optional<Tuple<T1, List<T2>>>> finisher() {
        return MultitupleBuilder::build;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return CHARACTERISTICS;
    }
}



class MultitupleBuilder<T1, T2> {

    private final OneTimeAssignment<T1> firstElement = OneTimeAssignment.newInstance();
    private final ConcurrentLinkedDeque<T2> secondElements = new ConcurrentLinkedDeque<>();

    private final BiFunction<? super Tuple<T1, List<T2>>, ? super Tuple<T1, Optional<T2>>, ? extends RuntimeException> exceptionOnNonDistinctFirstElement;

    MultitupleBuilder(BiFunction<? super Tuple<T1, List<T2>>, ? super Tuple<T1, Optional<T2>>, ? extends RuntimeException> exceptionOnNonDistinctFirstElement) {
        this.exceptionOnNonDistinctFirstElement = exceptionOnNonDistinctFirstElement;
    }

    void tryAccumulate(ViewableAsTuple<T1, Optional<T2>> hasTuple) {
        Tuple<T1, Optional<T2>> tuple = hasTuple.asTuple();
        try {
            firstElement.set(tuple.first());
        } catch (AlreadyAssigned e) {
            T1 assignedFirst = firstElement.get();
            if (!Objects.equals(assignedFirst, tuple.first())) {
                RuntimeException tooManyElements = exceptionOnNonDistinctFirstElement.apply(Tuple.of(assignedFirst, new ArrayList<>(secondElements)), tuple);
                tooManyElements.addSuppressed(e);
                throw tooManyElements;
            }
        }
        tuple.second().ifPresent(secondElements::add);
    }

    Optional<Tuple<T1, List<T2>>> build() {
        T1 first = firstElement.get();
        return first != null ? Optional.of(Tuple.of(first, new ArrayList<>(secondElements))) : Optional.empty();
    }

}

