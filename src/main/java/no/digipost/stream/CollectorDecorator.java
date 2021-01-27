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

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * Base {@link Collector} implementation which delegates to (decorates)
 * another Collector. Override individual methods to adapt any delegation.
 */
public abstract class CollectorDecorator<T, A, R> implements Collector<T, A, R> {

    protected final Collector<T, A, R> decoratedCollector;

    protected CollectorDecorator(Collector<T, A, R> collector) {
        this.decoratedCollector = collector;
    }

    @Override
    public Supplier<A> supplier() {
        return decoratedCollector.supplier();
    }

    @Override
    public BiConsumer<A, T> accumulator() {
        return decoratedCollector.accumulator();
    }

    @Override
    public BinaryOperator<A> combiner() {
        return decoratedCollector.combiner();
    }

    @Override
    public Function<A, R> finisher() {
        return decoratedCollector.finisher();
    }

    @Override
    public Set<Characteristics> characteristics() {
        return decoratedCollector.characteristics();
    }

}
