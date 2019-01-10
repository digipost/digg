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
package no.digipost;

import org.junit.jupiter.api.Test;
import org.quicktheories.WithQuickTheories;
import org.quicktheories.dsl.TheoryBuilder2;

import static no.digipost.DiggCompare.max;
import static no.digipost.DiggCompare.min;

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
}
