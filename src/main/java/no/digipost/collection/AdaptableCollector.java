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

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collector.Characteristics.IDENTITY_FINISH;
import static java.util.stream.Collectors.toSet;

/**
 * A {@link Collector} which offers some means for adapting it into new Collectors,
 * in particular the ability to {@link #andThen(Function) map the result}.
 *
 * @deprecated The functionality offered here can already be achieved by JDK classes.
 *             Use {@link Collectors#collectingAndThen(Collector, Function)} instead.
 */
@Deprecated
public final class AdaptableCollector<T, A, R> implements Collector<T, A, R> {

    @Deprecated
    public static <T, A, R> AdaptableCollector<T, A, R> of(Supplier<A> supplier,
                                                          BiConsumer<A, T> accumulator,
                                                          BinaryOperator<A> combiner,
                                                          Function<A, R> finisher,
                                                          Set<Characteristics> characteristics) {
        return new AdaptableCollector<>(supplier, accumulator, combiner, finisher, characteristics.stream());
    }

    @Deprecated
    public static <T, A, R> AdaptableCollector<T, A, R> of(Supplier<A> supplier,
                                                           BiConsumer<A, T> accumulator,
                                                           BinaryOperator<A> combiner,
                                                           Function<A, R> finisher,
                                                           Characteristics ... characteristics) {
        return new AdaptableCollector<>(supplier, accumulator, combiner, finisher, Stream.of(characteristics));
    }





    private final Supplier<A> supplier;
    private final BiConsumer<A, T> accumulator;
    private final BinaryOperator<A> combiner;
    private final Function<A, R> finisher;
    private final Set<Characteristics> characteristics;

    private AdaptableCollector(Supplier<A> supplier,
                               BiConsumer<A, T> accumulator,
                               BinaryOperator<A> combiner,
                               Function<A, R> finisher,
                               Stream<Characteristics> characteristics) {
        this.supplier = supplier;
        this.accumulator = accumulator;
        this.combiner = combiner;
        this.finisher = finisher;
        this.characteristics = unmodifiableSet(characteristics.collect(toSet()));
    }

    /**
     * Create a new {@code Collector} which maps the resulting container to something else
     * using the given {@code resultMapper} {@link Function}.
     *
     * @param <M> The new result type after applying the mapper functions.
     * @param resultMapper the function used to map the collector's result
     *
     * @return a new Collector
     * @deprecated Use {@link Collectors#collectingAndThen(Collector, Function)} instead.
     */
    @Deprecated
    public <M> AdaptableCollector<T, A, M> andThen(Function<? super R, M> resultMapper) {
        return new AdaptableCollector<>(supplier, accumulator, combiner, finisher.andThen(resultMapper), characteristics.stream().filter(c -> c != IDENTITY_FINISH));
    }

    @Override
    public Supplier<A> supplier() {
        return supplier;
    }

    @Override
    public BiConsumer<A, T> accumulator() {
        return accumulator;
    }

    @Override
    public BinaryOperator<A> combiner() {
        return combiner;
    }

    @Override
    public Function<A, R> finisher() {
        return finisher;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return characteristics;
    }

}
