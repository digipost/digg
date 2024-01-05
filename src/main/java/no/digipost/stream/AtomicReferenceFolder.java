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

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * A consumer function for "folding" a value with an {@link AtomicReference}.
 *
 * @param <T> The type of the value this function folds.
 */
@FunctionalInterface
interface AtomicReferenceFolder<T> extends BiConsumer<AtomicReference<T>, T> {

    static <T> AtomicReferenceFolder<T> clearReference() {
        return (ref, value) -> ref.set(null);
    }

    static <T> AtomicReferenceFolder<T> keepFirst(Predicate<? super T> predicate) {
        return (currentRef, candidateElement) ->
            currentRef.accumulateAndGet(candidateElement, (current, candidate) -> current == null && predicate.test(candidate) ? candidate : current);
    }

    static <T> AtomicReferenceFolder<T> keepLast(Predicate<? super T> predicate) {
        return (currentRef, candidateElement) -> {
            if (predicate.test(candidateElement)) {
                currentRef.set(candidateElement);
            }
        };
    }

    default AtomicReferenceFolder<T> doInsteadIf(Predicate<? super T> valuePredicate, AtomicReferenceFolder<T> foldOperation) {
        return doInsteadIf((ref, value) -> valuePredicate.test(value), foldOperation);
    }

    default AtomicReferenceFolder<T> doInsteadIf(BiPredicate<? super AtomicReference<T>, ? super T> refAndValuePredicate, AtomicReferenceFolder<T> foldOperation) {
        return (ref, value) -> {
            if (refAndValuePredicate.test(ref, value)) {
                foldOperation.accept(ref, value);
            } else {
                this.accept(ref, value);
            }
        };
    }
}
