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
package no.digipost.util;

import nl.jqno.equalsverifier.EqualsVerifier;
import no.digipost.tuple.Tuple;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

public class AttributeTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void settingAndGettingValues() {
        Map<String, Object> myAttributes = new HashMap<>();

        Attribute<String> name = new Attribute<>("name");
        Attribute<Integer> age = new Attribute<>("age");
        Attribute<LocalDate> birthday = new Attribute<>("birthday");

        name.setOn(myAttributes, "my name");
        age.setOn(myAttributes, 29);

        assertThat(name.getFrom(myAttributes), is(Optional.of("my name")));
        assertThat(age.getFrom(myAttributes), is(Optional.of(29)));
        assertThat(birthday.getFrom(myAttributes), is(empty()));

        assertThat(name.requireFrom(myAttributes), is("my name"));
        assertThat(age.requireFrom(myAttributes), is(29));

        expectedException.expect(Attribute.NotFound.class);
        expectedException.expectMessage(birthday.name);
        birthday.requireFrom(myAttributes);
    }

    @Test
    public void correctEqualsAndHashCode() {
        EqualsVerifier.forClass(Attribute.class).verify();
        assertThat(new Attribute<>("x"), is(new Attribute<>("x")));
    }

    @Test
    public void changeAttributeName() {
        Attribute<Long> num = new Attribute<>("num");
        Attribute<Long> longNum = num.withName("longNum");
        assertThat(num.name, is("num"));
        assertThat(longNum.name, is("longNum"));
        assertThat(num.withName("num"), sameInstance(num));
    }

    @Test
    public void coupleWithValue() {
        Tuple<Attribute<Long>, Long> boundAttribute = new Attribute<Long>("num").withValue(42L);
        assertThat(boundAttribute.first(), is(new Attribute<>("num")));
        assertThat(boundAttribute.second(), is(42L));
    }

}
