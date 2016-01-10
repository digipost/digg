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

import com.pholser.junit.quickcheck.ForAll;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.theories.Theories;
import org.junit.contrib.theories.Theory;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.util.Optional;

import static co.unruly.matchers.StreamMatchers.contains;
import static co.unruly.matchers.StreamMatchers.empty;
import static no.digipost.DiggBase.extract;
import static no.digipost.DiggBase.nonNull;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

@RunWith(Theories.class)
public class DiggBaseTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Theory(nullsAccepted = false)
    public void yieldsSameInstanceOnNonNullReferences(@ForAll String value) throws NullPointerException {
        assertThat(nonNull("my value", value), sameInstance(value));
        assertThat(nonNull(value, d -> d), sameInstance(value));
        assertThat(DiggBase.<String, NullPointerException>nonNull("my value", value, NullPointerException::new), sameInstance(value));
        assertThat(DiggBase.<String, NullPointerException>nonNull(value, d -> d, d -> new NullPointerException()), sameInstance(value));
    }

    @Test
    public void throwsNullPointerForNullReference() {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("my value");
        nonNull("my value", (Object) null);
    }

    @Test
    public void throwsCustomExceptionForNullReference() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("my value");
        nonNull("my value", (Object) null, IllegalStateException::new);
    }

    @Test
    public void throwsExceptionWithDescriptionInMessage() {
        String resourceName = "all/your/base/is/belong/to/us";
        expectedException.expectMessage(resourceName);
        nonNull(resourceName, getClass()::getResource);
    }

    @Test
    public void extractValuesFromAnObject() {
        assertThat(extract("abc", (String s) -> Optional.of(s.charAt(0)), s -> Optional.empty(), (String s) -> Optional.of(s.charAt(2))), contains('a', 'c'));
        assertThat(extract("abc", s -> Optional.empty(), s -> Optional.empty()), empty());
    }




}