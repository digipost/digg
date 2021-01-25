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

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static java.util.stream.Stream.iterate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;

public class NonEmptyStreamTest {

    @Test
    void reduceToSingleContainedValue() {
        String singleContainedValue = NonEmptyStream.of("x").reduceFromFirst((v1, v2) -> fail("Should not be invoked"));
        assertThat(singleContainedValue, is("x"));
    }

    @Test
    void reduceMultipleValues() {
        int sum = NonEmptyStream.of(1, Stream.of(2, 3, 4, 5)).reduceFromFirst((v1, v2) -> v1 + v2);
        assertThat(sum, is(15));
    }

    @Test
    void reduceMultipleValuesOfMappedStream() {
        int sumOfFactorials = NonEmptyStream.of(1, 2, 3, 4, 5)
                .map(n -> range(2, n).reduce(n, Math::multiplyExact))
                .reduce((f1, f2) -> f1 + f2).get();
        assertThat(sumOfFactorials, is(153));
    }

    @Test
    void sortMappedStream() {
        List<Integer> nums = NonEmptyStream.of("2", "3", "1").map(Integer::parseInt).sorted().collect(toList());
        assertThat(nums, contains(1, 2, 3));
    }

    @Test
    void limitStream() {
        List<String> strings = NonEmptyStream.of("x", iterate("xx", "x"::concat)).limitToNonEmpty(8).collect(toList());
        assertThat(strings, contains("x", "xx", "xxx", "xxxx", "xxxxx", "xxxxxx", "xxxxxxx", "xxxxxxxx"));
    }


}
