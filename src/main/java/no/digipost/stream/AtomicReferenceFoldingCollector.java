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

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collector.Characteristics.CONCURRENT;

final class AtomicReferenceFoldingCollector<T> implements Collector<T, AtomicReference<T>, Optional<T>> {

    private final AtomicReferenceFolder<T> accumulator;
    private final Set<Characteristics> characteristics;

    AtomicReferenceFoldingCollector(AtomicReferenceFolder<T> accumulator) {
        this(accumulator, EnumSet.of(CONCURRENT));
    }

    AtomicReferenceFoldingCollector(AtomicReferenceFolder<T> accumulator, Set<Characteristics> characteristics) {
        this.accumulator = accumulator;
        this.characteristics = unmodifiableSet(characteristics);
    }


    @Override
    public AtomicReferenceFolder<T> accumulator() {
        return accumulator;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return characteristics;
    }

    /**
     * The combiner, while thread-safe, has essentially undefined behavior if combining two
     * found elements, because there is no way to tell if one or the other should be prioritized.
     * In all cases of combining one found element with a not found, the found element will be
     * returned from the function.
     */
    @Override
    public BinaryOperator<AtomicReference<T>> combiner() {
        return (ref1, ref2) -> {
            ref1.compareAndSet(null, ref2.get());
            return ref1;
        };
    }

    @Override
    public Function<AtomicReference<T>, Optional<T>> finisher() {
        return ref -> Optional.ofNullable(ref.get());
    }

    @Override
    public Supplier<AtomicReference<T>> supplier() {
        return AtomicReference::new;
    }
}