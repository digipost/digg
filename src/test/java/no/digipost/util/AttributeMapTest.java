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

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static no.digipost.util.AttributesMap.buildNew;
import static no.digipost.util.AttributesMap.Config.ALLOW_NULL_VALUES;
import static no.digipost.util.DiggMatchers.isEffectivelySerializable;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@RunWith(JUnitQuickcheck.class)
public class AttributeMapTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Property
    public void attributeMapIsEmptyWhenSizeIsZero(List<String> attributeNames) {
        AttributesMap attributes = attributeNames.stream()
                .map(Attribute::new)
                .map(a -> a.withValue(42))
                .collect(AttributesMap::buildNew, AttributesMap.Builder::and, AttributesMap.Builder::and)
                .build();

        assertThat(attributes.isEmpty(), is(attributes.size() == 0));
    }

    @Property
    public void retrievesAttributeValueFor(String anyAttributeName, Long value) {
        Attribute<Long> myLong = new Attribute<>(anyAttributeName);
        AttributesMap attributes = AttributesMap.with(myLong.withValue(value)).build();
        assertThat(attributes.get(myLong), is(value));

        AttributesMap sameAttributes = AttributesMap.with(myLong, value).build();
        assertThat(attributes.get(myLong), is(sameAttributes.get(myLong)));
    }

    @Property
    public void stringRepresentation(Map<String, Integer> anyAttributes) {
        AttributesMap attributes = anyAttributes.entrySet().parallelStream().collect(
                () -> buildNew(ALLOW_NULL_VALUES),
                (builder, entry) -> builder.and(new Attribute<>(entry.getKey()), entry.getValue()),
                AttributesMap.Builder::and).build();

        AtomicInteger asserts = new AtomicInteger();
        anyAttributes.entrySet().forEach(e -> { assertThat(attributes.toString(), containsString(e.toString())); asserts.incrementAndGet(); });
        assertThat(asserts.get(), is(anyAttributes.size()));
    }


    @Test
    public void serializesAttributeWithNullValue() {
        AttributesMap attributes = AttributesMap.with(new Attribute<String>("a"), null, ALLOW_NULL_VALUES).build();
        assertThat(attributes.size(), is(1));
        assertThat(attributes, isEffectivelySerializable());
    }

    @Property
    public void isSerializable(Map<String, String> serializableAttributes) {
        AttributesMap attributes = serializableAttributes.entrySet().parallelStream().collect(
                () -> buildNew(ALLOW_NULL_VALUES),
                (builder, entry) -> builder.and(new Attribute<>(entry.getKey()), entry.getValue()),
                AttributesMap.Builder::and).build();
        assertThat(attributes, isEffectivelySerializable());
    }

    @Test
    public void correctEqualsHashcode() {
        EqualsVerifier.forClass(AttributesMap.class).verify();
    }


    private final Attribute<Integer> num = new Attribute<>("num");
    private final Attribute<Integer> anotherNum = new Attribute<>("anotherNum");
    private final Attribute<String> text = new Attribute<>("text");

    @Test
    public void combineTwoBuilders() {
        AttributesMap map = AttributesMap.with(num, 42).build();
        AttributesMap combinedMap = AttributesMap.with(anotherNum, 43).and(map).build();

        assertThat(map.size(), is(1));
        assertThat(map.get(num), is(42));

        assertThat(combinedMap.size(), is(2));
        assertThat(combinedMap.get(num), is(42));
        assertThat(combinedMap.get(anotherNum), is(43));
    }

    @Test
    public void distinguishNullValueAndNonExistingAttribute() {
        AttributesMap attributes = AttributesMap.with(text, null, ALLOW_NULL_VALUES).build();
        assertThat(attributes.get(text), nullValue());

        expectedException.expect(GetsNamedValue.NotFound.class);
        attributes.get(num);
    }

}
