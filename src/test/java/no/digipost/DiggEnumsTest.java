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
package no.digipost;

import org.junit.jupiter.api.Test;
import org.quicktheories.WithQuickTheories;
import org.quicktheories.core.Gen;

import java.util.function.Function;
import java.util.stream.Stream;

import static co.unruly.matchers.StreamMatchers.contains;
import static co.unruly.matchers.StreamMatchers.empty;
import static java.util.stream.Collectors.joining;
import static no.digipost.DiggEnums.fromCommaSeparatedNames;
import static no.digipost.DiggEnums.toCommaSeparatedNames;
import static no.digipost.DiggEnums.toNames;
import static no.digipost.DiggEnums.toStringOf;
import static no.digipost.DiggEnumsTest.MyEnum.A;
import static no.digipost.DiggEnumsTest.MyEnum.AA;
import static no.digipost.DiggEnumsTest.MyEnum.ABA;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class DiggEnumsTest implements WithQuickTheories {

    enum MyEnum {
        A, AA, ABA, ABC
    }

    private final Gen<MyEnum[]> multipleEnums = arrays().ofClass(arbitrary().enumValues(MyEnum.class), MyEnum.class).withLengthBetween(0, 30);

    @Test
    public void convertFromCommaSeparatedListOfEnumNames() {
        qt()
            .forAll(multipleEnums)
            .asWithPrecursor(DiggEnums::toCommaSeparatedNames)
            .checkAssert((enums, commaSeparatedNames) -> assertThat(fromCommaSeparatedNames(commaSeparatedNames, MyEnum.class), contains(enums)));

        qt()
            .forAll(multipleEnums)
            .asWithPrecursor(enums -> Stream.of(enums).map(Enum::name).collect(joining(" , ", "  ", "   ")))
            .checkAssert((enums, commaSeparatedNames) -> assertThat(fromCommaSeparatedNames(commaSeparatedNames, MyEnum.class), contains(enums)));

    }

    @Test
    public void noEnumsAreFoundInNullString() {
        assertThat(fromCommaSeparatedNames(null, MyEnum.class), empty());
    }

    @Test
    public void convertToStringOfDelimiterSeparatedStrings() {
        Function<? super MyEnum, String> lowerCasedEnumName = e -> e.name().toLowerCase();
        assertThat(toStringOf(lowerCasedEnumName, joining(": ", "[", "]"), A, ABA, AA), is("[a: aba: aa]"));
    }

    @Test
    public void toStringConversionsAreSpecialCasesOfTheGenericBaseCase() {
        qt()
            .forAll(multipleEnums)
            .check(enums -> toCommaSeparatedNames(enums).equals(toStringOf(Enum::name, joining(","), enums)));

        qt()
            .forAll(multipleEnums)
            .check(enums -> toNames(": ", enums).equals(toStringOf(Enum::name, joining(": "), enums)));

        qt()
            .forAll(multipleEnums)
            .check(enums -> toStringOf(e -> e.name().toLowerCase(), "#", enums).equals(toStringOf(e -> e.name().toLowerCase(), joining("#"), enums)));

    }

}
