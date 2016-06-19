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
package no.digipost.tuple;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class DecupleTest {

    @Test
    public void flattenNested10ElementTuple() {
        Tuple<Tuple<Tuple<Tuple<Tuple<Tuple<Tuple<Tuple<Tuple<Integer, Integer>, Integer>, Integer>, Integer>, Integer>, Integer>, Integer>, Integer>, Integer> _12345678910 =
                Tuple.of(Tuple.of(Tuple.of(Tuple.of(Tuple.of(Tuple.of(Tuple.of(Tuple.of(Tuple.of(1, 2), 3), 4), 5), 6), 7), 8), 9), 10);
        assertThat(Decuple.flatten(_12345678910), is(Decuple.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)));
    }

    @Test
    public void mapTenthElement() {
        Decuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> tenthIs11 = Decuple.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).mapTenth(n -> n + 1);
        assertThat(tenthIs11, is(Decuple.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 11)));
        assertThat(tenthIs11.tenth(), is(11));
    }

}
