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
package no.digipost.text;

import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static co.unruly.matchers.StreamMatchers.contains;
import static co.unruly.matchers.StreamMatchers.empty;
import static org.hamcrest.MatcherAssert.assertThat;

public class RegexTest {

    @Test
    public void extractGroups() {
        Pattern pattern = Pattern.compile("ab(\\d\\d)cd(\\d)");
        assertThat(Regex.extractGroups("ab12cd3", pattern), contains("12", "3"));
    }

    @Test
    public void noGroupsYieldsEmptyStream() {
        Pattern pattern = Pattern.compile(".*");
        assertThat(Regex.extractGroups("abc", pattern), empty());
    }

    @Test
    public void noMatchYieldsEmptyStream() {
        Pattern pattern = Pattern.compile("(a)");
        assertThat(Regex.extractGroups("b", pattern), empty());
    }

    @Test
    public void extractRepeatedlyOccuringGroups() {
        Pattern pattern = Pattern.compile("(^.|(?<=_).)[Y|A]");
        assertThat(Regex.extractGroups("MY_DATABASE_TABLE", pattern), contains("M", "D", "T"));
    }

}
