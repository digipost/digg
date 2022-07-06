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

import no.digipost.io.DataSize;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.lang.Math.toIntExact;
import static java.util.stream.Collectors.toList;
import static no.digipost.function.ThrowingFunction.identity;
import static no.digipost.util.bisect.Evaluator.having;
import static no.digipost.util.bisect.Evaluator.size;
import static no.digipost.util.bisect.Evaluator.Result.fromComparatorResult;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.quicktheories.QuickTheory.qt;
import static org.quicktheories.generators.SourceDSL.integers;

class BisectSearchTest {

    @Test
    void find14UsingBisection() {
        int result = BisectSearch.from(num -> Suggestion.of(num)).inRange(0, 1_000_000).searchFor(having(identity(), 14));
        assertThat(result, is(14));
    }

    @Test
    void findInstanceHavingValue14() {
        class A {
            final int value;
            A(int value) {
                this.value = value;
            }
            int value() {
                return value;
            }
        };
        A result = BisectSearch.from(num -> Suggestion.of(new A(num * 2))).inRange(0, 1_000_000).searchFor(having(A::value, 14));
        assertThat(result.value(), is(14));
    }


    @Test
    void findAmountOf45BytesChunksWhichGives512KiloBytes() {
        DataSize targetSize = DataSize.kB(512);
        DataSize chunkSize = DataSize.bytes(45);
        List<byte[]> result = BisectSearch
            .from(chunks -> Suggestion.of(Stream.generate(() -> new byte[toIntExact(chunkSize.toBytes())]).limit(chunks).collect(toList())))
            .inRange(1, 1_000_000).maximumAttempts(20)
            .searchFor(size(targetSize, (chunks, output) -> chunks.forEach(output::write)));

        assertThat(result, hasSize(toIntExact(targetSize.toBytes() / chunkSize.toBytes())));
    }


    @Test
    void allAutoCloseableSuggestionsAreClosedExceptTheResult() throws Exception {
        List<AutoCloseable> allSuggestions = new ArrayList<>();
        Object result = BisectSearch
            .from(i -> {
                AutoCloseable nextAutoCloseable = mock(AutoCloseable.class);
                allSuggestions.add(nextAutoCloseable);
                return Suggestion.of((Object) nextAutoCloseable);
            })
            .inRange(0, 100)
            .searchFor(o -> Evaluator.Result.TOO_LOW);
        assertAll(allSuggestions.subList(0, allSuggestions.size() - 1).stream().map(suggestion -> () -> verify(suggestion, times(1)).close()));

        AutoCloseable lastSuggestion = allSuggestions.get(allSuggestions.size() - 1);
        assertThat(lastSuggestion, sameInstance(result));
        verify(lastSuggestion, never()).close();
    }


    @Nested
    class Ranges {
        final BisectSearch.Builder<Integer> fromNumbers = BisectSearch.from(num -> Suggestion.of(num));

        @Test
        void emptyRangeIsNotAllowed() {
            qt()
                .forAll(integers().all())
                .checkAssert(minAndMax -> assertThrows(IllegalArgumentException.class, () -> fromNumbers.inRange(minAndMax, minAndMax)));
        }

        @Test
        void lowerBoundMustBeLessThanUpperBound() {
            qt()
                .forAll(integers().allPositive())
                .checkAssert(min -> assertThrows(IllegalArgumentException.class, () -> fromNumbers.inRange(min, min - 1)));
        }

        @Test
        void aRangeIncludesLowerBoundButExcludesUpperBound() {
            BisectSearch<Integer> rangeClosedFromZeroTo1000 = BisectSearch.from(num -> Suggestion.of(num)).inRange(0, 1_000).maximumAttempts(100);
            int thousand = rangeClosedFromZeroTo1000.searchFor(suggestion -> fromComparatorResult(suggestion.compareTo(1_000)));
            int zero = rangeClosedFromZeroTo1000.searchFor(suggestion -> fromComparatorResult(suggestion.compareTo(0)));
            assertAll(
                    () -> assertThat(thousand, is(999)),
                    () -> assertThat(zero, is(0)));
        }
    }


}
