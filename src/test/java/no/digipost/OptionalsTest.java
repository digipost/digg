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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;
import java.util.Optional;

import static no.digipost.Optionals.toList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;

public class OptionalsTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void convertOptionalToList() {
        assertThat(toList(Optional.of(42)), contains(42));
        assertThat(toList(Optional.empty()), empty());
    }

    @Test
    public void cannotAddToReturnedList() {
        List<Integer> list = toList(Optional.of(42));

        expectedException.expect(UnsupportedOperationException.class);
        list.add(43);
    }

    @Test
    public void cannotRemoveFromReturnedList() {
        List<Integer> list = toList(Optional.of(42));

        expectedException.expect(UnsupportedOperationException.class);
        list.clear();
    }
}
