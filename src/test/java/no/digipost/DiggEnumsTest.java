/*
 * Copyright (C) Posten Bring AS
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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.quicktheories.core.Gen;

import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static no.digipost.DiggEnums.fromCommaSeparatedNames;
import static no.digipost.DiggEnums.selectByIndexAsOrdinals;
import static no.digipost.DiggEnums.toCommaSeparatedNames;
import static no.digipost.DiggEnums.toNames;
import static no.digipost.DiggEnums.toStringOf;
import static no.digipost.DiggEnumsTest.MyEnum.A;
import static no.digipost.DiggEnumsTest.MyEnum.AA;
import static no.digipost.DiggEnumsTest.MyEnum.ABA;
import static no.digipost.DiggEnumsTest.MyEnum.ABC;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.quicktheories.QuickTheory.qt;
import static org.quicktheories.generators.SourceDSL.arbitrary;
import static org.quicktheories.generators.SourceDSL.arrays;
import static uk.co.probablyfine.matchers.StreamMatchers.contains;
import static uk.co.probablyfine.matchers.StreamMatchers.empty;

class DiggEnumsTest {

    enum MyEnum {
        A, AA, ABA, ABC
    }
    enum Empty {
    }

    private static final Gen<MyEnum[]> multipleEnums = arrays().ofClass(arbitrary().enumValues(MyEnum.class), MyEnum.class).withLengthBetween(0, 30);

    @Nested
    static class FromString {

        @Test
        void convertFromCommaSeparatedListOfEnumNames() {
            qt()
                .forAll(multipleEnums)
                .asWithPrecursor(DiggEnums::toCommaSeparatedNames)
                .checkAssert((enums, commaSeparatedNames) -> assertThat(fromCommaSeparatedNames(commaSeparatedNames, MyEnum.class), contains(enums)));

            qt()
                .forAll(multipleEnums)
                .asWithPrecursor(enums -> Stream.of(enums).map(MyEnum::name).collect(joining(" , ", "  ", "   ")))
                .checkAssert((enums, commaSeparatedNames) -> assertThat(fromCommaSeparatedNames(commaSeparatedNames, MyEnum.class), contains(enums)));

        }

        @Test
        void noEnumsAreFoundInNullString() {
            assertThat(fromCommaSeparatedNames(null, MyEnum.class), empty());
        }
    }


    @Nested
    static class ToString {

        @Test
        void convertToStringOfDelimiterSeparatedStrings() {
            Function<? super MyEnum, String> lowerCasedEnumName = e -> e.name().toLowerCase();
            assertThat(toStringOf(lowerCasedEnumName, joining(": ", "[", "]"), A, ABA, AA), is("[a: aba: aa]"));
        }

        @Test
        void toStringConversionsAreSpecialCasesOfTheGenericBaseCase() {
            qt()
                .forAll(multipleEnums)
                .check(enums -> toCommaSeparatedNames(enums).equals(toStringOf(MyEnum::name, joining(","), enums)));

            qt()
                .forAll(multipleEnums)
                .check(enums -> toNames(": ", enums).equals(toStringOf(MyEnum::name, joining(": "), enums)));

            qt()
                .forAll(multipleEnums)
                .check(enums -> toStringOf(e -> e.name().toLowerCase(), "#", enums).equals(toStringOf(e -> e.name().toLowerCase(), joining("#"), enums)));
        }
    }


    @Nested
    static class ResolveFromBooleanArray {

        @Test
        void noBooleansYieldsNoEmums() {
            assertThat(selectByIndexAsOrdinals(new boolean[0], MyEnum.class), empty());
        }

        @Test
        void emptyEnumYieldsYieldsNoEmums() {
            assertThat(selectByIndexAsOrdinals(new boolean[0], Empty.class), empty());
            assertThat(selectByIndexAsOrdinals(new boolean[] {true}, Empty.class), empty());
            assertThat(selectByIndexAsOrdinals(new boolean[] {false}, Empty.class), empty());
            assertThat(selectByIndexAsOrdinals(new boolean[] {false, true}, Empty.class), empty());
            assertThat(selectByIndexAsOrdinals(new boolean[] {true, true, false}, Empty.class), empty());
        }

        @Test
        void resolveSelectionBasedOnBooleanArray() {
            assertThat(selectByIndexAsOrdinals(new boolean[] {true, false, false, true}, MyEnum.class), contains(A, ABC));
            assertThat(selectByIndexAsOrdinals(new boolean[] {true, false, false, true, true}, MyEnum.class), contains(A, ABC));
            assertThat(selectByIndexAsOrdinals(new boolean[] {true, false, false}, MyEnum.class), contains(A));
            assertThat(selectByIndexAsOrdinals(new boolean[] {true, true, false}, MyEnum.class), contains(A, AA));
        }
    }

}
