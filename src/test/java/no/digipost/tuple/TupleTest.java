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
import org.quicktheories.WithQuickTheories;
import org.quicktheories.core.Gen;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;

public class TupleTest implements WithQuickTheories {

    private final Gen<Object> anything = arbitrary()
            .pick(new Object())
            .mix(strings().allPossible().ofLengthBetween(0, 100).map(s -> s), 90)
            .mix(integers().all().map(i -> i), 50);

    @Test
    public void accessElements() {
        qt()
            .forAll(anything, anything)
            .asWithPrecursor(Tuple::of)
            .checkAssert((first, second, tuple) -> {
                assertThat(tuple.first(), sameInstance(first));
                assertThat(tuple.second(), sameInstance(second));
            });
    }

    @Test
    public void stringRepresentationIncludesBothElements() {
        qt()
            .forAll(anything, anything)
            .asWithPrecursor(Tuple::of)
            .checkAssert((first, second, tuple) -> {
                assertThat(tuple.toString(), both(containsString(String.valueOf(first))).and(containsString(String.valueOf(second))));
            });
    }

    @Test
    public void asTupleReturnsSameInstance() {
        qt()
            .forAll(anything.zip(anything, Tuple::of))
            .check(tuple -> tuple.asTuple() == tuple);
    }

    @Test
    public void mapFirstElement() {
        assertThat(Tuple.of("1", 2).mapFirst(Integer::parseInt), is(Tuple.of(1, 2)));
    }

    @Test
    public void mapSecondElement() {
        assertThat(Tuple.of(1, "2").mapSecond(Integer::parseInt), is(Tuple.of(1, 2)));
    }

    @Test
    public void swapping() {
        assertThat(Tuple.of("a", 'b').swap(), is(Tuple.of('b', "a")));
    }

}
