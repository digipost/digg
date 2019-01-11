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
import org.junit.jupiter.api.Test;
import org.quicktheories.WithQuickTheories;
import org.quicktheories.core.Gen;

import java.util.Map;

import static co.unruly.matchers.Java8Matchers.where;
import static no.digipost.util.AttributesMap.Config.ALLOW_NULL_VALUES;
import static no.digipost.util.DiggMatchers.isEffectivelySerializable;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class AttributesMapTest implements WithQuickTheories {

    private final Gen<Attribute<Integer>> attributes() {
        return strings().ascii().ofLengthBetween(0, 40).map(Attribute::new);
    }

    private Gen<Map<Attribute<Integer>, Integer>> attributesInMaps() {
        return maps().of(attributes(), integers().all()).ofSizeBetween(0, 40);
    }

    private final Gen<AttributesMap> attributesMaps = attributesInMaps().map(AttributesMapTest::toAttributesMap);


    @Test
    public void attributeMapIsEmptyWhenSizeIsZero() {
        qt()
            .forAll(attributesMaps)
            .check(map -> (map.size() == 0) == map.isEmpty());
    }

    @Test
    public void retrievesAttributeValues() {
        qt()
            .forAll(attributes(), integers().all())
            .check((attribute, value) -> AttributesMap.with(attribute.withValue(value)).build().get(attribute).equals(value));

        qt()
            .forAll(attributes(), integers().all())
            .check((attribute, value) -> AttributesMap.with(attribute, value).build().get(attribute).equals(value));
    }

    @Test
    public void stringRepresentation() {
        qt()
            .forAll(attributesInMaps())
            .asWithPrecursor(AttributesMapTest::toAttributesMap)
            .checkAssert((attributes, attributesMap) -> attributes
                    .forEach((attr, value) -> assertThat(attributesMap.toString(), both(containsString(attr.name.toString())).and(containsString(value.toString())))));
    }


    @Test
    public void serializesAttributeWithNullValue() {
        AttributesMap attributes = AttributesMap.with(new Attribute<String>("a"), null, ALLOW_NULL_VALUES).build();
        assertThat(attributes, where(AttributesMap::size, is(1)));
        assertThat(attributes, isEffectivelySerializable());
    }

    @Test
    public void isSerializable() {
        qt()
            .forAll(attributesMaps)
            .checkAssert(map -> assertThat(map, isEffectivelySerializable()));
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

        assertThrows(GetsNamedValue.NotFound.class, () -> attributes.get(num));
    }

    private static <V> AttributesMap toAttributesMap(Map<? extends SetsNamedValue<V>, V> namesAndValues) {
        return namesAndValues.entrySet().stream().map(e -> Tuple.of(e.getKey(), e.getValue())).collect(AttributesMap::buildNew, AttributesMap.Builder::and, AttributesMap.Builder::and).build();
    }

}
