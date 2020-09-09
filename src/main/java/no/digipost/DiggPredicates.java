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
package no.digipost;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

public final class DiggPredicates {

    /**
     * Create a new predicate which only yields true on the <em>nth</em>
     * {@code true}-yielding invocation of a given predicate.
     *
     * <p>
     * As this is a stateful predicate, it should usually not be kept as a
     * reference, but used inline when processing a number of elements,
     * with {@link java.util.stream.Stream#filter streams}.
     *
     * @param n the nth {@code true}-yielding invocation, {@code 1} indicates first,
     *          {@code 2} second, and so forth. {@code 0} or less will throw an
     *          {@code IllegalArgumentException}.
     * @param predicate the predicate.
     * @return the new predicate.
     */
    public static <T> Predicate<T> nth(long n, Predicate<T> predicate) {
        if (n <= 0) {
            throw new IllegalArgumentException(n + " is not a valid number for n. It must be 1 or greater");
        }
        AtomicLong foundMatches = new AtomicLong();
        return t -> predicate.test(t) && foundMatches.incrementAndGet() == n;
    }

    private DiggPredicates() {}
}
