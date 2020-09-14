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

import java.util.HashMap;
import java.util.Map;

import static co.unruly.matchers.StreamMatchers.contains;
import static org.hamcrest.MatcherAssert.assertThat;

class TuplesTest {

    @Test
    void convertMapToTuples() {
        Map<Integer, String> map = new HashMap<>();
        map.put(1, "one");
        map.put(2, "two");
        assertThat(Tuples.ofMap(map), contains(Tuple.of(1, "one"), Tuple.of(2, "two")));
    }

}
