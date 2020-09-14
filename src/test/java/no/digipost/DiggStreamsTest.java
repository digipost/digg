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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class DiggStreamsTest implements WithQuickTheories {

    @Test
    public void streamAllElementsByIntIndex() {
        Gen<List<String>> listsOfStrings = lists().of(strings().allPossible().ofLengthBetween(0, 100)).ofSizeBetween(0, 20);
        qt()
            .forAll(listsOfStrings)
            .checkAssert(list -> assertThat(streamByIntIndex(list, list.size(), List::get).collect(toList()), is(list)));
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

    @Test
    public void streamPagesWhilePageHasContent() {
        Gen<Integer> alphabetLengths = integers().between(10, 200);
        Gen<Integer> pageSizes = integers().between(1, 13);

        qt()
            .forAll(alphabetLengths, pageSizes)
            .as(PagedAlphabet::new)
            .checkAssert(pagedAlphabet -> assertThat(streamWhileNonEmpty(pagedAlphabet::getPage), equalTo(pagedAlphabet.getEntireAlphabet())));
    }

    private static final class PagedAlphabet {
        final char firstChar = 'A';
        final int length;
        final int pageSize;

        public PagedAlphabet(int length, int pageSize) {
            this.length = length;
            this.pageSize = pageSize;
        }

        Collection<Character> getPage(int page) {
            return iterate(firstChar + pageSize * page, chr -> chr + 1)
                    .limit(pageSize)
                    .filter(chr -> chr < firstChar + length)
                    .mapToObj(chr -> Character.valueOf((char) chr))
                    .collect(toList());
        }

        Stream<Character> getEntireAlphabet() {
            return iterate(firstChar, chr -> chr + 1).limit(length).mapToObj(chr -> Character.valueOf((char) chr));
        }
    }
}
