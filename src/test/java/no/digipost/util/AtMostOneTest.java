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
package no.digipost.util;

import no.digipost.concurrent.OneTimeToggle;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AtMostOneTest {


    @Test
    public void picksOutFirstElementAndDiscardsRemaining() {
        assertThat(AtMostOne.from((Iterable<Integer>) asList(1, 2)).discardRemaining().get(), is(1));
    }

    @Test
    public void noElementsYieldsEmptyOptional() {
        assertThat(AtMostOne.from(emptyList()).discardRemaining(), is(Optional.empty()));
        assertThat(AtMostOne.from(emptyList()).<AssertionError>orIfExcessiveThrow(AssertionError::new), is(Optional.empty()));
    }

    @Test
    public void picksOutFirstElementInSingleElementList() {
        assertThat(AtMostOne.from(singleton("a")).toOptional().get(), is("a"));
        assertThat(AtMostOne.from(singleton(null)).toOptional(), is(Optional.empty()));
    }

    @Test
    public void handleExcessiveElements() {
        OneTimeToggle excessiveElements = new OneTimeToggle();
        AtMostOne.from(asList("a", "b")).orElse(excessiveElements::now);
        assertTrue(excessiveElements.yet());

        OneTimeToggle excessiveNull = new OneTimeToggle();
        AtMostOne.from(asList("a", null)).orElse(excessiveNull::now);
        assertTrue(excessiveElements.yet());
    }

    @Test
    public void throwsExceptionsIfRemainingItemsWhishWouldBeDiscarded() {
        AtMostOne<String> atMostOne = AtMostOne.from(asList("a", "b"));

        RuntimeException e = new RuntimeException();
        assertThat(assertThrows(RuntimeException.class, () -> atMostOne.orIfExcessiveThrow(() -> e)), sameInstance(e));
    }

    @Test
    public void throwsInternalExceptionIfMultipleElements() {
        List<String> elements = asList("a", "b");
        AtMostOne<String> atMostOne = AtMostOne.from(elements);

        assertThrows(ViewableAsOptional.TooManyElements.class, atMostOne::toOptional);
    }
}
