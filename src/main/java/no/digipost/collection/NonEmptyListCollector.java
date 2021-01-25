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

import no.digipost.stream.CollectorDecorator;
import no.digipost.stream.EmptyIfEmptySourceCollector;

import java.util.Optional;

final class NonEmptyListCollector<T, A>
extends CollectorDecorator<T, A, Optional<NonEmptyList<T>>>
implements EmptyIfEmptySourceCollector<T, A, NonEmptyList<T>> {

    NonEmptyListCollector(java.util.stream.Collector<T, A, Optional<NonEmptyList<T>>> collector) {
        super(collector);
    }

}