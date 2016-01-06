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

import no.digipost.tuple.Tuple;
import no.digipost.tuple.ViewableAsTuple;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static no.digipost.DiggCollectors.toMultimap;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;

public class DiggCollectorsTest {

    interface NumTranslation extends ViewableAsTuple<Integer, Optional<String>> {}

    @Test
    public void collateTuplesIntoMap() {
        NumTranslation oneInEnglish = () -> Tuple.of(1, Optional.of("one"));
        NumTranslation twoInEnglish = () -> Tuple.of(2, Optional.of("two"));
        NumTranslation twoInSpanish = () -> Tuple.of(2, Optional.of("dos"));
        NumTranslation oneInSpanish = () -> Tuple.of(1, Optional.of("uno"));
        NumTranslation noTranslationsForFive = () -> Tuple.of(5, Optional.empty());


        Stream<NumTranslation> translations = Stream.<NumTranslation>builder().add(oneInEnglish).add(twoInSpanish).add(oneInSpanish).add(twoInEnglish).add(noTranslationsForFive).build();

        Map<Integer, List<String>> byNumber = translations.collect(toMultimap());
        assertThat(byNumber.get(1), containsInAnyOrder("one", "uno"));
        assertThat(byNumber.get(2), containsInAnyOrder("two", "dos"));
        assertThat(byNumber.get(5), empty());
    }
}
