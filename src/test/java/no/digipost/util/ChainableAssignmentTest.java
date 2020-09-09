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
package no.digipost.util;

import no.digipost.concurrent.OneTimeAssignment;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ChainableAssignmentTest {

    @Test
    public void canBeUsedToImplementBuilderStyleObjectPopulation() {
        int builtSum = new Builder()
            .first.is(42)
            .second.is(1295)
            .sum();
        assertThat(builtSum, is(1337));

        int builtUsingDefaultFirstValue = new Builder()
            .second.is(2)
            .sum();
        assertThat(builtUsingDefaultFirstValue, is(3));
    }

}

class Builder {
    final ChainableAssignment<Integer, Builder> first = OneTimeAssignment.defaultTo(1).chainableWith(this);
    final ChainableAssignment<Integer, Builder> second = Assignment.from(new AtomicReference<Integer>()).chainableWith(this);

    int sum() {
        return first.get() + second.get();
    }
}
