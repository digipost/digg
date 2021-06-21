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
package no.digipost.collection;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.co.probablyfine.matchers.OptionalMatchers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static no.digipost.DiggCollectors.toNonEmptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.quicktheories.QuickTheory.qt;
import static org.quicktheories.generators.SourceDSL.integers;
import static org.quicktheories.generators.SourceDSL.lists;
import static org.quicktheories.generators.SourceDSL.strings;
import static uk.co.probablyfine.matchers.Java8Matchers.where;
import static uk.co.probablyfine.matchers.Java8Matchers.whereNot;

public class NonEmptyListTest {

    @Test
    void streamAndCollectBackToNonEmptyList() {
        NonEmptyList<Integer> sortedStringLengths = NonEmptyList.of("xy", "xyz", "x").stream().map(String::length).sorted().collect(toNonEmptyList());
        assertThat(sortedStringLengths, contains(1, 2, 3));
    }

    @Test
    void streamAndFilterCollectsToOptionalNonEmptyList() {
        Optional<NonEmptyList<String>> sortedStringLengths = NonEmptyList.of("xy", "xyz", "x").stream().filter(s -> s.length() > 1).collect(toNonEmptyList());
        assertThat(sortedStringLengths, OptionalMatchers.contains(containsInAnyOrder("xy", "xyz")));
    }

    @Test
    void singleElement() {
        assertThat(NonEmptyList.of("x"), where(NonEmptyList::isSingular));
        assertThat(NonEmptyList.of("x"), whereNot(NonEmptyList::hasMultipleElements));
    }

    @Test
    void moreThanOneElement() {
        assertThat(NonEmptyList.of("x", "y"), where(NonEmptyList::hasMultipleElements));
        assertThat(NonEmptyList.of("x", "y"), whereNot(NonEmptyList::isSingular));
    }

    @Test
    void neverEmptyInstanceOfNonEmptyList() {
        qt()
            .forAll(
                    lists().of(strings().allPossible().ofLengthBetween(0, 20)).ofSizes(integers().between(1, 20))
                    .map(list -> NonEmptyList.of(list))
                    .assuming(Optional::isPresent)
                    .map(Optional::get))
            .checkAssert(list -> assertThat(list, whereNot(NonEmptyList::isEmpty)));

        assertThat(NonEmptyList.of(emptyList()), is(Optional.empty()));
    }

    @Nested
    class Construction {
        @Test
        void unsafeThrowsIfEmpty() {
            assertThrows(IllegalArgumentException.class, () -> NonEmptyList.ofUnsafe(new Object[0]));
            assertThrows(IllegalArgumentException.class, () -> NonEmptyList.ofUnsafe(emptyList()));
            assertThrows(IllegalArgumentException.class, () -> NonEmptyList.copyOfUnsafe(new Object[0]));
            assertThrows(IllegalArgumentException.class, () -> NonEmptyList.copyOfUnsafe(emptyList()));
        }

        @Test
        void copyConstructorsAreNotAffectedByChangesInSource() {
            NonEmptyList<Character> copyOfArray = constructAndMutateSourceArray(source -> NonEmptyList.copyOf(source).get(), 'a', 'b', 'c');
            assertThat(copyOfArray, contains('a', 'b', 'c'));
            NonEmptyList<Character> copyOfList = constructAndMutateSourceList(source -> NonEmptyList.copyOf(source).get(), 'a', 'b', 'c');
            assertThat(copyOfList, contains('a', 'b', 'c'));
            NonEmptyList<Character> copyOfArrayUnsafe = constructAndMutateSourceArray(NonEmptyList::copyOfUnsafe, 'a', 'b', 'c');
            assertThat(copyOfArrayUnsafe, contains('a', 'b', 'c'));
            NonEmptyList<Character> copyOfListUnsafe = constructAndMutateSourceList(NonEmptyList::copyOfUnsafe, 'a', 'b', 'c');
            assertThat(copyOfListUnsafe, contains('a', 'b', 'c'));
        }

        @Test
        void changesInSourceWillMutateListsFromNonCopyingConstructors() {
            NonEmptyList<Character> viewOfMutatedArray = constructAndMutateSourceArray(source -> NonEmptyList.of(source).get(), 'a', 'b', 'c');
            assertThat(viewOfMutatedArray, not(contains('a', 'b', 'c')));
            NonEmptyList<Character> viewOfMutatedList = constructAndMutateSourceList(source -> NonEmptyList.of(source).get(), 'a', 'b', 'c');
            assertThat(viewOfMutatedList, not(contains('a', 'b', 'c')));
            NonEmptyList<Character> viewOfMutatedArrayUnsafe = constructAndMutateSourceArray(NonEmptyList::ofUnsafe, 'a', 'b', 'c');
            assertThat(viewOfMutatedArrayUnsafe, not(contains('a', 'b', 'c')));
            NonEmptyList<Character> viewOfMutatedListUnsafe = constructAndMutateSourceList(NonEmptyList::ofUnsafe, 'a', 'b', 'c');
            assertThat(viewOfMutatedListUnsafe, not(contains('a', 'b', 'c')));
        }


        private NonEmptyList<Character> constructAndMutateSourceArray(Function<Character[], NonEmptyList<Character>> listConstructor, Character ... chars) {
            Character[] sourceArray = Arrays.copyOf(chars, chars.length);
            NonEmptyList<Character> nonEmptyList = listConstructor.apply(sourceArray);
            assertThat(nonEmptyList, contains(chars));
            for (int i = 0; i < sourceArray.length; i++) {
                sourceArray[i] = (char) (sourceArray[i] + sourceArray.length);
            }
            assertAll(Stream.of(chars).map(c -> () -> assertThat(sourceArray, not(hasItemInArray(c)))));
            return nonEmptyList;
        }

        private NonEmptyList<Character> constructAndMutateSourceList(Function<List<Character>, NonEmptyList<Character>> listConstructor, Character ... chars) {
            List<Character> sourceList = new ArrayList<>(asList(chars));
            NonEmptyList<Character> nonEmptyList = listConstructor.apply(sourceList);
            assertThat(nonEmptyList, contains(chars));
            for (int i = 0; i < sourceList.size(); i++) {
                sourceList.set(i, (char) (sourceList.get(i) + sourceList.size()));
            }
            assertAll(Stream.of(chars).map(c -> () -> assertThat(sourceList, not(hasItem(c)))));
            return nonEmptyList;
        }

    }

}
