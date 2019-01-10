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

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class TripleTest {

    @Test
    public void flattenNested3ElementTuple() {
        Tuple<Tuple<Integer, Integer>, Integer> oneTwoThree = Tuple.of(Tuple.of(1, 2), 3);
        assertThat(Triple.flatten(oneTwoThree), is(Triple.of(1, 2, 3)));
    }

    @Test
    public void mapThirdElement() {
        Triple<Integer, Integer, Integer> thirdIs4 = Triple.of(1, 2, 3).mapThird(n -> n + 1);
        assertThat(thirdIs4, is(Triple.of(1, 2, 4)));
        assertThat(thirdIs4.third(), is(4));
    }

}
