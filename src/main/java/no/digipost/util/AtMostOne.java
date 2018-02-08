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
package no.digipost.util;

import no.digipost.DiggCollectors;
import no.digipost.function.ThrowingRunnable;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.Supplier;


/**
 * This class offers a subtle functionality which is not available in the
 * Collection API of Java: to retrieve the only element that is expected to be present,
 * and, importantly,
 * {@link #orIfExcessiveThrow(Supplier) throw an exception if there are elements that will be discarded}
 * if assuming that there is at most one element present.
 * <p>
 * If this funcitonality is needed for {@link java.util.stream.Stream streams}, use the using {@link DiggCollectors#allowAtMostOne()}
 * collector instead with {@link java.util.stream.Stream#collect(java.util.stream.Collector) Stream.collect(..)}.
 *
 * @param <T> The type of the contained object.
 */
public interface AtMostOne<T> extends ViewableAsOptional<T> {

    public static <T> AtMostOne<T> from(Iterable<T> iterable) {
        return new AtMostOne<T>() {
            @Override
            public <X extends Throwable> Optional<T> orElse(ThrowingRunnable<X> handleUnexpectedMultipleElements) throws X {
                Iterator<T> iterator = iterable.iterator();
                final Optional<T> expected = iterator.hasNext() ? Optional.ofNullable(iterator.next()) : Optional.empty();
                if (iterator.hasNext()) {
                    handleUnexpectedMultipleElements.run();
                }
                return expected;
            }
        };
    }



    /**
     * This allows to just pick out the first element, and basically offers the same functionality
     * as {@link java.util.stream.Stream#findFirst()}.
     *
     * @return The first element if it exists, or {@link Optional#empty()} if no elements exist.
     */
    default Optional<T> discardRemaining() {
        return orElse(() -> {});
    }

    /**
     * Get the at most single contained element, or throw a {@link TooManyElements} exception
     * if there are excessive elements available. If you need control over the thrown exception, use
     * {@link #orIfExcessiveThrow(Supplier)}.
     *
     * @return The single element if it exists, or else {@link Optional#empty()} if no elements exist or
     *         the single element is {@code null}.
     */
    @Override
    default Optional<T> toOptional() {
        return orIfExcessiveThrow(ViewableAsOptional.TooManyElements::new);
    }


    /**
     * Pick out the single contained element, or throw the given exception if there are
     * excessive (more than one) elements available.
     *
     *
     * @param exceptionSupplier the exception to throw if there are excessive elements available.
     * @return The single element if it exists, or {@link Optional#empty()} if no elements exist.
     *
     * @throws X if there are excessive elements available.
     */
    default <X extends Throwable> Optional<T> orIfExcessiveThrow(Supplier<X> exceptionSupplier) throws X {
        return orElse(() -> { throw exceptionSupplier.get(); });
    }


    /**
     * How to handle if there actually are multiple elements available when only at most one is expected.
     *
     * @param handleUnexpectedMultipleElements the handling of the unexpected multiple elements.
     * @return The first element if it exists, or {@link Optional#empty()} if no elements exist.
     * @throws X if the function to handle more than one elements throws an exception.
     */
    <X extends Throwable> Optional<T> orElse(ThrowingRunnable<X> handleUnexpectedMultipleElements) throws X;

}
