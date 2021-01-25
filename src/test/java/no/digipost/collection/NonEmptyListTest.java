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

import co.unruly.matchers.OptionalMatchers;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static co.unruly.matchers.Java8Matchers.where;
import static co.unruly.matchers.Java8Matchers.whereNot;
import static java.util.Collections.emptyList;
import static no.digipost.collection.NonEmptyList.toNonEmptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.quicktheories.QuickTheory.qt;
import static org.quicktheories.generators.SourceDSL.integers;
import static org.quicktheories.generators.SourceDSL.lists;
import static org.quicktheories.generators.SourceDSL.strings;

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

}
