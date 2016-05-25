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

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.generator.InRange;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.util.function.Predicate;
import java.util.stream.Stream;

import static no.digipost.DiggPredicates.nth;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(JUnitQuickcheck.class)
public class DiggPredicatesTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Property
    public void yieldsTrueOnGivenInvocationNumber(@InRange(minInt=1, maxInt=1024) int invocationNum) {
        Predicate<Integer> isNthEvenNumber = nth(invocationNum, t -> t % 2 == 0);
        Integer nthEvenNumber = Stream.iterate(1, i -> i + 1).limit(invocationNum * 2).filter(isNthEvenNumber).findFirst().get();
        assertThat(nthEvenNumber, is(invocationNum * 2));
    }

    @Property
    public void zeroAndNegativeNumbersAreInvalidForNth(@InRange(maxInt=0) int invalidInvocationNum) {
        expectedException.expect(IllegalArgumentException.class);
        nth(invalidInvocationNum, t -> true);
    }

}
