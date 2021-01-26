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
package no.digipost.collection;

import no.digipost.concurrent.OneTimeAssignment;
import no.digipost.concurrent.OneTimeAssignment.AlreadyAssigned;
import no.digipost.stream.EmptyIfEmptySourceCollector;
import no.digipost.util.ViewableAsOptional;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Collections.unmodifiableSet;

public class EnforceAtMostOneElementCollector<T> implements EmptyIfEmptySourceCollector<T, OneTimeAssignment<T>, T> {

    private static final Set<Characteristics> CHARACTERISTICS = unmodifiableSet(EnumSet.of(Characteristics.CONCURRENT));

    private final BiFunction<? super T, ? super T, ? extends RuntimeException> exceptionOnExcessiveElements;

    public EnforceAtMostOneElementCollector(BiFunction<? super T, ? super T, ? extends RuntimeException> exceptionOnExcessiveElements) {
        this.exceptionOnExcessiveElements = exceptionOnExcessiveElements;
    }

    @Override
    public Supplier<OneTimeAssignment<T>> supplier() {
        return OneTimeAssignment::newInstance;
    }

    @Override
    public BiConsumer<OneTimeAssignment<T>, T> accumulator() {
        return this::trySet;
    }

    @Override
    public BinaryOperator<OneTimeAssignment<T>> combiner() {
        return (a1, a2) -> {
            if (a1.isSet() && a2.isSet()) {
                throw exceptionOnExcessiveElements.apply(a1.get(), a2.get());
            } else if (a1.isSet()) {
                return a1;
            } else {
                return a2;
            }
        };
    }

    @Override
    public Function<OneTimeAssignment<T>, Optional<T>> finisher() {
        return ViewableAsOptional::toOptional;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return CHARACTERISTICS;
    }



    private void trySet(OneTimeAssignment<T> ref, T value) {
        try {
            ref.set(value);
        } catch (AlreadyAssigned e) {
            RuntimeException tooManyElements = exceptionOnExcessiveElements.apply(ref.get(), value);
            tooManyElements.addSuppressed(e);
            throw tooManyElements;
        }
    }

}
