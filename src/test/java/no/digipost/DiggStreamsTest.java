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
import com.pholser.junit.quickcheck.generator.InRange;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Stream;

import static co.unruly.matchers.StreamMatchers.contains;
import static co.unruly.matchers.StreamMatchers.equalTo;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.IntStream.iterate;
import static no.digipost.DiggStreams.streamByIntIndex;
import static no.digipost.DiggStreams.streamByKey;
import static no.digipost.DiggStreams.streamByLongIndex;
import static no.digipost.DiggStreams.streamWhileNonEmpty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(JUnitQuickcheck.class)
public class DiggStreamsTest {

    @Property
    public void streamAllElementsByIntIndex(List<String> anyListOfStrings) {
        assertThat(streamByIntIndex(anyListOfStrings, anyListOfStrings.size(), List::get).collect(toList()), is(anyListOfStrings));
    }

    @Test
    public void streamValuesOfMap() {
        List<Character> chars = asList('A', 'B', 'C', 'D');
        Map<Character, String> charAndString = chars.stream().collect(toMap(Function.identity(), String::valueOf));
        assertThat(streamByKey(charAndString, chars.stream(), Map::get), contains("A", "B", "C", "D"));
    }

    @Test
    public void streamValuesByLongIndex() {
        assertThat(streamByLongIndex(new LongConverter(), 4, LongConverter::asString), contains("0", "1", "2", "3"));
    }

    static class LongConverter {
        String asString(long value) {
            return String.valueOf(value);
        }
    }

    @Property
    public void streamPagesWhilePageHasContent(@InRange(minInt = 10, maxInt = 200) int alphabetLength, @InRange(minInt = 1, maxInt = 13) int pageSize) {
        IntFunction<Collection<Character>> pagedAlphabet = page -> iterate('A' + pageSize * page, chr -> chr + 1)
                .limit(pageSize)
                .filter(chr -> chr < 'A' + alphabetLength)
                .mapToObj(chr -> Character.valueOf((char) chr))
                .collect(toList());

        Stream<Character> expectedAlphabet = iterate('A', chr -> chr + 1).limit(alphabetLength).mapToObj(chr -> Character.valueOf((char) chr));

        assertThat(streamWhileNonEmpty(pagedAlphabet), equalTo(expectedAlphabet));
    }
}