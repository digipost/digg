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

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@RunWith(JUnitQuickcheck.class)
public class TupleTest {

    @Property
    public void accessElements(Object first, Object second) {
        Tuple<?, ?> tuple = Tuple.of(first, second);
        assertThat(tuple.first(), sameInstance(first));
        assertThat(tuple.second(), sameInstance(second));
    }

    @Property
    public void stringRepresentationIncludesBothElements(Object first, Object second) {
        Tuple<?, ?> tuple = Tuple.of(first, second);
        assertThat(tuple.toString(), both(containsString(String.valueOf(first))).and(containsString(String.valueOf(second))));
    }

    @Test
    public void asTupleReturnsSameInstance() {
        Tuple<?, ?> tuple = Tuple.of(1, 2);
        assertThat(tuple.asTuple(), sameInstance(tuple));
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
