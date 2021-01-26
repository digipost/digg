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

import static uk.co.probablyfine.matchers.StreamMatchers.contains;
import static java.lang.Integer.MIN_VALUE;
import static java.util.stream.Stream.iterate;
import static no.digipost.DiggBase.forceOnAll;
import static no.digipost.DiggPredicates.nth;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

public class DiggPredicatesTest implements WithQuickTheories {

    @Test
    public void yieldsTrueOnGivenInvocationNumber() {
        qt()
            .forAll(integers().between(1, 1024))
            .asWithPrecursor(n -> iterate(1, i -> i + 1).filter(nth(n, this::isEven)).findFirst().get())
            .check((evenNumberOrdinal, evenNumber) -> evenNumber == evenNumberOrdinal * 2);
    }

    @Test
    public void zeroAndNegativeNumbersAreInvalidForNth() {
        qt()
            .forAll(integers().between(MIN_VALUE, 0))
            .as(negativeOrdinal -> forceOnAll(n -> nth(n, t -> true), negativeOrdinal))
            .checkAssert(error -> assertThat(error, contains(instanceOf(IllegalArgumentException.class))));
    }

    private boolean isEven(int n) {
        return n % 2 == 0;
    }

}
