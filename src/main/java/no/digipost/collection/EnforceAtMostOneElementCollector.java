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
import no.digipost.util.ViewableAsOptional;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static java.util.Collections.unmodifiableSet;

public class EnforceAtMostOneElementCollector<T> implements Collector<T, OneTimeAssignment<T>, Optional<T>> {

    private static final Set<Characteristics> CHARACTERISTICS = unmodifiableSet(EnumSet.of(Characteristics.CONCURRENT));

    @Override
    public Supplier<OneTimeAssignment<T>> supplier() {
        return OneTimeAssignment::newInstance;
    }

    @Override
    public BiConsumer<OneTimeAssignment<T>, T> accumulator() {
        return EnforceAtMostOneElementCollector::trySet;
    }

    @Override
    public BinaryOperator<OneTimeAssignment<T>> combiner() {
        return (a1, a2) -> {
            T a2Value = a2.get();
            if (a2Value != null) {
                trySet(a1, a2Value);
            }
            return a1;
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



    private static <T> void trySet(OneTimeAssignment<T> ref, T value) {
        try {
            ref.set(value);
        } catch (AlreadyAssigned e) {
            throw new ViewableAsOptional.TooManyElements(ref.get(), value, e);
        }
    }

}
