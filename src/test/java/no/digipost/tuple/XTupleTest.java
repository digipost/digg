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

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static java.math.BigDecimal.ONE;
import static java.util.Arrays.asList;
import static no.digipost.util.DiggMatchers.isEffectivelySerializable;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;

class XTupleTest {
    @Test
    void correctEqualsAndHashCode() {
        EqualsVerifier.forClass(XTuple.class).verify();
    }

    @Test
    void isSerializable() {
        assertThat(new XTuple<>("x", 2, XTuple.TERMINATOR, null, null, null, null, null, null, null), isEffectivelySerializable());
    }

    @Test
    void converting() {
        XTuple<String, Integer, BigDecimal, URI, String, UUID, List<String>, Integer, URI, Long> xTuple =
                new XTuple<>("x", 1, ONE, URI.create("my/resource"), "y", UUID.nameUUIDFromBytes("z".getBytes()), asList("a", "b"), 42, URI.create("."), 1L);

        assertCorrectConversion(xTuple.asTuple().to(Compound::new), xTuple, Tuple.class);
        assertCorrectConversion(xTuple.asTriple().to(Compound::new), xTuple, Triple.class);
        assertCorrectConversion(xTuple.asQuadruple().to(Compound::new), xTuple, Quadruple.class);
        assertCorrectConversion(xTuple.asPentuple().to(Compound::new), xTuple, Pentuple.class);
        assertCorrectConversion(xTuple.asHextuple().to(Compound::new), xTuple, Hextuple.class);
        assertCorrectConversion(xTuple.asSeptuple().to(Compound::new), xTuple, Septuple.class);
        assertCorrectConversion(xTuple.asOctuple().to(Compound::new), xTuple, Octuple.class);
        assertCorrectConversion(xTuple.asNonuple().to(Compound::new), xTuple, Nonuple.class);
        assertCorrectConversion(xTuple.asDecuple().to(Compound::new), xTuple, Decuple.class);
    }

    private static void assertCorrectConversion(
            Compound compound, XTuple<?, Integer, BigDecimal, URI, String, UUID, List<String>, Integer, URI, Long> xTuple, Class<?> tupleType) {

        assertAll(Stream.<Executable>of(
                () -> assertThat(compound.text, is(xTuple.first())),
                () -> assertThat(compound.number, is(xTuple.second())),
                () -> assertThat(compound.bigNumber, is(xTuple.third())),
                () -> assertThat(compound.uri, is(xTuple.fourth())),
                () -> assertThat(compound.anotherText, is(xTuple.fifth())),
                () -> assertThat(compound.id, is(xTuple.sixth())),
                () -> assertThat(compound.strings, is(xTuple.seventh())),
                () -> assertThat(compound.yetAnotherNumber, is(xTuple.eighth())),
                () -> assertThat(compound.secondUri, is(xTuple.ninth())),
                () -> assertThat(compound.arbitraryObject, is(xTuple.tenth()))
        ).limit(tupleType.getTypeParameters().length));
    }
}
