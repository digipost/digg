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

import no.digipost.function.ThrowingRunnable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;


/**
 * This class offers a subtle functionality which is not available in the
 * Collection/Stream API of Java: to retrieve the only element that is expected to be present,
 * and, importantly,
 * {@link #orIfExcessiveThrow(Supplier) throw an exception if there are elements that will be discarded}
 * if assuming that there is at most one element present.
 *
 * @param <T> The type of the contained object.
 */
public interface AtMostOne<T> {

    public static final class TooManyElements extends RuntimeException {
        public TooManyElements() {
            super("exepcted at most one element, but there were excess ones");
        }
    }


    /**
     * This allows to just pick out the first element, and basically offers the same functionality
     * as {@link Stream#findFirst()}.
     *
     * @return The first element if it exists, or {@link Optional#empty()} if no elements exist.
     */
    default Optional<T> discardRemaining() {
        return orElse(() -> {});
    }

    /**
     * Get the at most single contained element, or throw a {@link TooManyElements} exception
     * if there are excessive elements available. If you need controll over the thrown exception, use
     * {@link #orIfExcessiveThrow(Supplier)}
     *
     * @return The single element if it exists, or {@link Optional#empty()} if no elements exist.
     */
    default Optional<T> toOptional() {
        return orIfExcessiveThrow(TooManyElements::new);
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
     * @param handleUnexpectedMultipleElements the handling of the unexpected multiple elements. The stream
     *                                         passed to this function will <em>always</em> contain at least
     *                                         two elements (the expected first element, and the excess ones).
     * @return The first element if it exists, or {@link Optional#empty()} if no elements exist.
     * @throws X if the function to handle more than one elements throws an exception.
     */
    <X extends Throwable> Optional<T> orElse(ThrowingRunnable<X> handleUnexpectedMultipleElements) throws X;



    public static <T> AtMostOne<T> from(Iterable<T> iterable) {
        return from(StreamSupport.stream(iterable.spliterator(), false));
    }

    public static <T> AtMostOne<T> from(Collection<T> list) {
        return from(list.stream());
    }

    public static <T> AtMostOne<T> from(Stream<T> stream) {
        return new AtMostOne<T>() {

            @Override
            public <X extends Throwable> Optional<T> orElse(ThrowingRunnable<X> handleUnexpectedMultipleElements) throws X{
                List<T> possiblyTwoItems = stream.limit(2).collect(toList());
                int size = possiblyTwoItems.size();
                if (size == 0) {
                    return Optional.empty();
                } else if (size > 1) {
                    handleUnexpectedMultipleElements.run();
                }
                return Optional.of(possiblyTwoItems.get(0));
            }

        };
    }

}
