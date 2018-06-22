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
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.function.Function;

import static co.unruly.matchers.StreamMatchers.empty;
import static co.unruly.matchers.StreamMatchers.equalTo;
import static com.pholser.junit.quickcheck.Mode.EXHAUSTIVE;
import static java.util.stream.Collectors.joining;
import static no.digipost.DiggEnums.fromCommaSeparatedNames;
import static no.digipost.DiggEnums.toCommaSeparatedNames;
import static no.digipost.DiggEnums.toNames;
import static no.digipost.DiggEnums.toStringOf;
import static no.digipost.DiggEnumsTest.MyEnum.A;
import static no.digipost.DiggEnumsTest.MyEnum.AA;
import static no.digipost.DiggEnumsTest.MyEnum.ABA;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(JUnitQuickcheck.class)
public class DiggEnumsTest {

    enum MyEnum {
        A, AA, ABA, ABC
    }

    @Property(mode = EXHAUSTIVE)
    public void convertFromCommaSeparatedListOfEnumNames(List<MyEnum> enums) {
        assertThat(fromCommaSeparatedNames(enums.stream().map(Enum::name).collect(joining(",")), MyEnum.class), equalTo(enums.stream()));
        assertThat(fromCommaSeparatedNames(enums.stream().map(Enum::name).collect(joining(" , ", "  ", "   ")), MyEnum.class), equalTo(enums.stream()));
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

    @Property(mode = EXHAUSTIVE)
    public void toStringConversionsAreSpecialCasesOfTheGenericBaseCase(MyEnum ... enums) {
        assertThat(toCommaSeparatedNames(enums), is(toStringOf(Enum::name, joining(","), enums)));
        assertThat(toNames(": ", enums), is(toStringOf(Enum::name, joining(": "), enums)));
        assertThat(toStringOf(e -> e.name().toLowerCase(), "#", enums), is(toStringOf(e -> e.name().toLowerCase(), joining("#"), enums)));
    }

}
