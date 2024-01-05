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
import java.util.function.Predicate;
import java.util.stream.Collector;

import static java.util.function.Predicate.isEqual;
import static no.digipost.stream.AtomicReferenceFolder.clearReference;
import static no.digipost.stream.AtomicReferenceFolder.keepFirst;
import static no.digipost.stream.AtomicReferenceFolder.keepLast;

/**
 * The initial subject filter for building a searching {@code Collector}.
 * The {@link Collector} is acquired by invoking a method to finalize the
 * (compound) condition for accumulating the result across the elements
 * the collector will be applied to.
 *
 * @param <T> the type of elements this subject filter inspects
 *
 * @see #keepFirstNotFollowedBy(Predicate)
 * @see #keepLastNotFollowedBy(Predicate)
 */
public final class SubjectFilter<T> {
    private final Predicate<T> subjectElement;

    public SubjectFilter(Predicate<T> subjectElement) {
        this.subjectElement = subjectElement;
    }

    public Collector<T, ?, Optional<T>> keepFirstNotFollowedBy(T cancellingElement) {
        return keepFirstNotFollowedBy(isEqual(cancellingElement));
    }

    public Collector<T, ?, Optional<T>> keepFirstNotFollowedBy(Predicate<? super T> cancellingElement) {
        return new AtomicReferenceFoldingCollector<>(keepFirst(subjectElement).doInsteadIf(cancellingElement, clearReference()));
    }

    public Collector<T, ?, Optional<T>> keepLastNotFollowedBy(T cancellingElement) {
        return keepLastNotFollowedBy(isEqual(cancellingElement));
    }

    public Collector<T, ?, Optional<T>> keepLastNotFollowedBy(Predicate<? super T> cancellingElement) {
        return new AtomicReferenceFoldingCollector<>(keepLast(subjectElement).doInsteadIf(cancellingElement, clearReference()));
    }
}
