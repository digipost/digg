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
 * {@link #orThrowIfAnyRemaining(Supplier) throw an exception if there are elements that will be discarded}
 * if assuming that there is at most one element present.
 *
 * @param <T> The type of the contained object.
 */
public abstract class AtMostOne<T> {

    /**
     * This allows to just pick out the first element, and basically offers the same functionality
     * as {@link Stream#findFirst()}.
     *
     * @return The first element if it exists, or {@link Optional#empty()} if no elements exist.
     */
    public abstract Optional<T> discardRemaining();


    /**
     * Pick out the single contained element, or throw the given exception if there are
     * multiple elements available.
     *
     *
     * @param exceptionSupplier the exception to throw if there are multiple elements available.
     * @return The single element if it exists, or {@link Optional#empty()} if no elements exist.
     *
     * @throws X if there are multiple elements available.
     */
    public abstract <X extends Throwable> Optional<T> orThrowIfAnyRemaining(Supplier<X> exceptionSupplier) throws X;


    public static <T> AtMostOne<T> from(Iterable<T> iterable) {
        return from(StreamSupport.stream(iterable.spliterator(), false));
    }

    public static <T> AtMostOne<T> from(Collection<T> list) {
        return from(list.stream());
    }

    public static <T> AtMostOne<T> from(Stream<T> stream) {
        return new AtMostOne<T>() {

            @Override
            public Optional<T> discardRemaining() {
                return stream.findFirst();
            }

            @Override
            public <X extends Throwable> Optional<T> orThrowIfAnyRemaining(Supplier<X> exceptionSupplier) throws X {
                List<T> possiblyTwoItems = stream.limit(2).collect(toList());
                int size = possiblyTwoItems.size();
                if (size > 1) {
                    throw exceptionSupplier.get();
                } else if (size == 1) {
                    return Optional.of(possiblyTwoItems.get(0));
                } else {
                    return Optional.empty();
                }
            }

        };
    }


    private AtMostOne() {}
}
