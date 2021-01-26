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

import uk.co.probablyfine.matchers.OptionalMatchers;
import uk.co.probablyfine.matchers.StreamMatchers;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static no.digipost.DiggExceptions.mayThrow;
import static no.digipost.DiggOptionals.toList;
import static no.digipost.DiggOptionals.toStream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DiggOptionalsTest {

    @Test
    public void convertOptionalToList() {
        assertThat(toList(Optional.of(42)), contains(42));
        assertThat(toList(Optional.empty()), empty());
    }

    @Test
    public void cannotAddToReturnedList() {
        List<Integer> list = toList(Optional.of(42));

        assertThrows(UnsupportedOperationException.class, () -> list.add(43));
    }

    @Test
    public void cannotRemoveFromReturnedList() {
        List<Integer> list = toList(Optional.of(42));

        assertThrows(UnsupportedOperationException.class, list::clear);
    }

    @Test
    public void convertPresentOptionalsToStream() {
        assertThat(toStream(Optional.empty(), Optional.empty()), StreamMatchers.empty());
        assertThat(toStream(Optional.empty(), Optional.of(1), Optional.of(2), Optional.empty()), StreamMatchers.contains(1, 2));
    }

    @Test
    public void getPresentOptionalsInStream() {
        Stream<Object> onlyNonPresentOptionals = toStream(() -> Optional.empty(), () -> Optional.empty());
        assertThat(onlyNonPresentOptionals, StreamMatchers.empty());

        Stream<Integer> nonPresentOptionalsAndOneAndTwo = toStream(() -> Optional.empty(), () -> Optional.of(1), () -> Optional.of(2), () -> Optional.empty());
        assertThat(nonPresentOptionalsAndOneAndTwo, StreamMatchers.contains(1, 2));
    }

    @Test
    public void convertPresentOptionalsToStreamOfMostCommonBaseType() {
        Stream<Number> nums = toStream(Optional.of(3.14), Optional.empty(), Optional.of(42));
        assertThat(nums.map(n -> String.format("%.2f", n.doubleValue())), StreamMatchers.contains("3.14", "42.00"));
    }

    @Test
    public void getPresentOptionalsInStreamOfMostCommonBaseType() {
        Stream<Number> nums = toStream(() -> Optional.of(3.14), Optional::empty, () -> Optional.of(42));
        assertThat(nums.map(n -> String.format("%.2f", n.doubleValue())), StreamMatchers.contains("3.14", "42.00"));
    }

    @Test
    public void getFirstPresentOptionalWillNotInvokeRemainingResolvers() {
        assertThat(toStream(() -> Optional.empty(), () -> Optional.of(1), () -> { throw new AssertionError(); }, () -> { throw new AssertionError(); }, () -> { throw new AssertionError(); }).findFirst(), OptionalMatchers.contains(1));
    }

    @Test
    public void parallelGetAnyOfPresentOptionalWillYieldTheEarliestResolvedOptional() {
        Supplier<Optional<Integer>> longRunningOne = mayThrow(() -> {
            Thread.sleep(1000);
            return Optional.of(1); }
        ).asUnchecked();

        Supplier<Optional<Integer>> two = () -> Optional.of(2);

        assertThat(toStream(longRunningOne, two, two, two, two, two, two, two).parallel().findAny(), OptionalMatchers.contains(2));
    }

}
