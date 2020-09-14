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
package no.digipost.tuple;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class PentupleTest {

    @Test
    public void flattenNested5ElementTuple() {
        Tuple<Tuple<Tuple<Tuple<Integer, Integer>, Integer>, Integer>, Integer> oneTwoThreeFourFive = Tuple.of(Tuple.of(Tuple.of(Tuple.of(1, 2), 3), 4), 5);
        assertThat(Pentuple.flatten(oneTwoThreeFourFive), is(Pentuple.of(1, 2, 3, 4, 5)));
    }

    @Test
    public void mapFifthElement() {
        Pentuple<Integer, Integer, Integer, Integer, Integer> fifthIs6 = Pentuple.of(1, 2, 3, 4, 5).mapFifth(n -> n + 1);
        assertThat(fifthIs6, is(Pentuple.of(1, 2, 3, 4, 6)));
        assertThat(fifthIs6.fifth(), is(6));
    }

}
