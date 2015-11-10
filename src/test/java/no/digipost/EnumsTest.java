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
import org.junit.Test;
import org.junit.contrib.theories.Theories;
import org.junit.contrib.theories.Theory;
import org.junit.runner.RunWith;

import java.util.List;

import static co.unruly.matchers.StreamMatchers.empty;
import static co.unruly.matchers.StreamMatchers.equalTo;
import static java.util.stream.Collectors.joining;
import static no.digipost.Enums.fromCommaSeparatedNames;
import static org.junit.Assert.assertThat;

@RunWith(Theories.class)
public class EnumsTest {

    enum MyEnum {
        A, AA, ABA, ABC
    }

    @Theory
    public void convertFromCommaSeparatedListOfEnumNames(@ForAll List<MyEnum> enums) {
        assertThat(fromCommaSeparatedNames(enums.stream().map(Enum::name).collect(joining(",")), MyEnum.class), equalTo(enums.stream()));
        assertThat(fromCommaSeparatedNames(enums.stream().map(Enum::name).collect(joining(",", "  ", "   ")), MyEnum.class), equalTo(enums.stream()));
    }

    @Test
    public void noEnumsFoundInNullString() {
        assertThat(fromCommaSeparatedNames(null, MyEnum.class), empty());
    }

}
