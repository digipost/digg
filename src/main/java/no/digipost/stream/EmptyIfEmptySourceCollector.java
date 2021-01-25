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

import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Stream;

/**
 * A collector type specific for implementations which produce an {@link Optional}
 * based on if the source (typically a {@link Stream}, or more apropriately,
 * a {@link NonEmptyStream}) yields no elements or any element(s). Typically appropriate
 * for reduction operations which needs at least one element to produce a valid result.
 *
 * @param <T> the type of input elements to the reduction operation
 * @param <R> the type of value contained in the resulting Optional, if not source is empty.
 */
public interface EmptyIfEmptySourceCollector<T, A, R> extends Collector<T, A, Optional<R>> {

    static <T, A, R> EmptyIfEmptySourceCollector<T, A, R> from(Collector<T, A, Optional<R>> collector) {
        if (collector instanceof EmptyIfEmptySourceCollector) {
            return (EmptyIfEmptySourceCollector<T, A, R>) collector;
        }
        class Impl extends CollectorDecorator<T, A, Optional<R>> implements EmptyIfEmptySourceCollector<T, A, R> {
            Impl(Collector<T, A, Optional<R>> collector) {
                super(collector);
            }
        }
        return new Impl(collector);
    }

}
