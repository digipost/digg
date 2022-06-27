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
package no.digipost.util.bisect;

import static no.digipost.util.bisect.Evaluator.Result.FOUND;
import static no.digipost.util.bisect.Evaluator.Result.TOO_HIGH;

public final class BisectSearch<T> {

    public static <T> BisectSearch.Builder<T> from(Suggester<T> suggester) {
        return new BisectSearch.Builder<>(suggester);
    }

    public static final class Builder<T> {
        /**
         * The amount of attempts needed to find a specific value
         * among one million ordered distinct values.
         */
        private static final int DEFAULT_MAX_ATTEMPTS = 20;
        private final Suggester<T> suggester;

        private Builder(Suggester<T> suggester) {
            this.suggester = suggester;
        }


        /**
         * Specify the range of points to base the search on, using
         * a range from an inclusive lower bound, up to, but not including, upper bound.
         *
         * @param min the inclusive lower bound
         * @param max the exclusive upper bound
         *
         * @return the {@code BisectSearch} instance which can be used to perform searches
         */
        public BisectSearch<T> inRange(int min, int max) {
            if (min == max) {
                throw new IllegalArgumentException("min and max specifies an empty range of [" + min + "," + max + ")");
            } else if (min > max) {
                throw new IllegalArgumentException("min " + min + " larger than max " + max);
            }
            return new BisectSearch<>(min, max, DEFAULT_MAX_ATTEMPTS, suggester);
        }
    }


    @FunctionalInterface
    public interface Disposer<T> {
        static final Disposer<Object> NO_DISPOSING = suggestion -> {};
        static final Disposer<AutoCloseable> DISPOSE_BY_CLOSING = AutoCloseable::close;

        void dispose(T suggestion) throws Exception;
    }



    private final int min;
    private final int max;
    private final int maxAttempts;
    private final Suggester<T> suggester;

    private BisectSearch(int min, int max, int maxAttempts, Suggester<T> suggester) {
        this.min = min;
        this.max = max;
        this.maxAttempts = maxAttempts;
        this.suggester = suggester;
    }

    public BisectSearch<T> maximumAttempts(int maxAttempts) {
        return new BisectSearch<>(min, max, maxAttempts, suggester);
    }

    public T searchFor(Evaluator<? super T> evaluator) {
        return bisect(min, max, suggester, evaluator, maxAttempts);
    }


    private static <T> T bisect(int min, int max, Suggester<T> suggester, Evaluator<? super T> evaluator, int attempts) {
        if (attempts < 1) {
            throw new IllegalArgumentException("Must be allowed at least 1, but was only allowed " + attempts + " suggestion attempts");
        }
        int mid = (min + max) / 2;
        Evaluator.Result result;
        try (Suggestion<? extends T> suggestion = suggester.suggest(mid)) {
            if (mid == min || mid == max || attempts == 1) {
                return suggestion.accepted();
            }

            try {
                result = evaluator.evaluate(suggestion.peek());
            } catch (Exception e) {
                throw new RuntimeException(
                        "Unable to evaluate suggested " + suggestion + ", created from bisection midpoint value " + mid +
                        ", because " + e.getClass().getSimpleName() + ": '" + e.getMessage() + "'", e);
            }
            if (result == FOUND) {
                return suggestion.accepted();
            }
        } catch (Exception e) {
            throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(
                    "Unable to process suggestion from bisection midpoint value " + mid +
                    ", because " + e.getClass().getSimpleName() + ": '" + e.getMessage() + "'", e);
        }

        if (result == TOO_HIGH) {
            return bisect(min, mid, suggester, evaluator, attempts - 1);
        } else {
            return bisect(mid, max, suggester, evaluator, attempts - 1);
        }
    }

}
