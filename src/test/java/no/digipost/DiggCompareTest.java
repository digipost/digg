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

import org.junit.jupiter.api.Test;
import org.quicktheories.WithQuickTheories;
import org.quicktheories.core.Gen;
import org.quicktheories.dsl.TheoryBuilder2;

import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.reverseOrder;
import static java.util.Comparator.comparing;
import static java.util.stream.Stream.concat;
import static no.digipost.DiggCompare.max;
import static no.digipost.DiggCompare.maxBy;
import static no.digipost.DiggCompare.min;
import static no.digipost.DiggCompare.minBy;
import static no.digipost.DiggCompare.prioritize;
import static no.digipost.DiggCompare.prioritizeIf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.quicktheories.generators.Generate.pick;
import static uk.co.probablyfine.matchers.StreamMatchers.contains;
import static uk.co.probablyfine.matchers.StreamMatchers.equalTo;
import static uk.co.probablyfine.matchers.StreamMatchers.startsWith;

public class DiggCompareTest implements WithQuickTheories {

    @Test
    public void minAndMax() {
        TheoryBuilder2<Integer, Integer> forNonEqualsInts = qt()
            .forAll(integers().all(), integers().all())
            .assuming((x, y) -> !x.equals(y));

        forNonEqualsInts.check((x, y) -> min(x, y) != max(x, y));
        forNonEqualsInts.check((x, y) -> min(x, y) == min(y, x));
        forNonEqualsInts.check((x, y) -> max(x, y) == max(y, x));
    }

    enum Num {
        ZERO, ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT;

        boolean isEven() {
            return ordinal() % 2 == 0;
        }
    }

    @Test
    void minAndMaxByProperty() {
        Gen<Num> anyNum = pick(asList(Num.values()));
        TheoryBuilder2<Num, Num> forNonEqualsNums = qt()
                .forAll(anyNum, anyNum)
                .assuming((x, y) -> !x.equals(y));

        forNonEqualsNums.check((x, y) -> maxBy(Num::ordinal, x, y) != minBy(Num::ordinal, x, y));
        forNonEqualsNums.check((x, y) -> minBy(Num::ordinal, x, y) == minBy(Num::ordinal, y, x));
        forNonEqualsNums.check((x, y) -> maxBy(Num::ordinal, x, y) == maxBy(Num::ordinal, y, x));
    }

    @Test
    void prioritizeCertainElements() {
        assertThat(Stream.of(Num.values()).sorted(prioritizeIf(emptyList())), equalTo(Stream.of(Num.values())));
        assertThat(Stream.of(Num.values()).sorted(prioritize()), equalTo(Stream.of(Num.values())));

        assertThat(
                Stream.of(Num.values()).sorted(prioritize(Num.SEVEN)),
                startsWith(Num.SEVEN, Num.ZERO, Num.ONE, Num.TWO));

        assertThat(
                concat(Stream.of(Num.values()), Stream.of((Num) null))
                    .sorted(prioritize(Num.SEVEN, null).thenComparing(prioritize(Num.FOUR).reversed())),
                contains(Num.SEVEN, null, Num.ZERO, Num.ONE, Num.TWO, Num.THREE, Num.FIVE, Num.SIX, Num.EIGHT, Num.FOUR));

        assertThat(
                Stream.of(Num.values()).sorted(comparing(Num::ordinal, prioritize(7, 4))),
                startsWith(Num.SEVEN, Num.FOUR, Num.ZERO, Num.ONE, Num.TWO));

        assertThat(Stream.of(Num.values()).sorted(prioritizeIf(asList(Num::isEven, Num.SEVEN::equals))),
                contains(Num.ZERO, Num.TWO, Num.FOUR, Num.SIX, Num.EIGHT, Num.SEVEN, Num.ONE, Num.THREE, Num.FIVE));

        assertThat(Stream.of(Num.values()).sorted(prioritizeIf(Num::isEven, Num.ONE::equals).thenComparing(reverseOrder())),
                contains(Num.EIGHT, Num.SIX, Num.FOUR, Num.TWO, Num.ZERO, Num.ONE, Num.SEVEN, Num.FIVE, Num.THREE));

    }


}
