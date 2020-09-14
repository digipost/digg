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

public class SeptupleTest {

    @Test
    public void flattenNested7ElementTuple() {
        Tuple<Tuple<Tuple<Tuple<Tuple<Tuple<Integer, Integer>, Integer>, Integer>, Integer>, Integer>, Integer> _1234567 =
                Tuple.of(Tuple.of(Tuple.of(Tuple.of(Tuple.of(Tuple.of(1, 2), 3), 4), 5), 6), 7);
        assertThat(Septuple.flatten(_1234567), is(Septuple.of(1, 2, 3, 4, 5, 6, 7)));
    }

    @Test
    public void mapSeventhElement() {
        Septuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer> seventhIs8 = Septuple.of(1, 2, 3, 4, 5, 6, 7).mapSeventh(n -> n + 1);
        assertThat(seventhIs8, is(Septuple.of(1, 2, 3, 4, 5, 6, 8)));
        assertThat(seventhIs8.seventh(), is(8));
    }

}
