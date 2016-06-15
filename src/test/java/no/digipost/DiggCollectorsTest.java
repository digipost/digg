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

import co.unruly.matchers.OptionalMatchers;
import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.When;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import no.digipost.collection.ConflictingElementEncountered;
import no.digipost.tuple.Tuple;
import no.digipost.tuple.ViewableAsTuple;
import no.digipost.util.ViewableAsOptional;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static co.unruly.matchers.OptionalMatchers.contains;
import static no.digipost.DiggCollectors.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@RunWith(JUnitQuickcheck.class)
public class DiggCollectorsTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    interface NumTranslation extends ViewableAsTuple<Integer, Optional<String>> {}

    @Test
    public void collectTuplesIntoMap() {
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

    @Test
    public void collectMultitupleFromTuplesWithEqualFirstElement() {
        NumTranslation oneInEnglish = () -> Tuple.of(1, Optional.of("one"));
        NumTranslation oneInSpanish = () -> Tuple.of(1, Optional.of("uno"));
        NumTranslation oneInEsperanto = () -> Tuple.of(1, Optional.of("unu"));
        NumTranslation missingOne = () -> Tuple.of(1, Optional.empty());

        Stream<NumTranslation> translations = Stream.<NumTranslation>builder().add(oneInEnglish).add(oneInSpanish).add(missingOne).add(oneInEsperanto).build();
        Tuple<Integer, List<String>> collectedTranslations = translations.collect(toMultituple()).get();
        assertThat(collectedTranslations.first(), is(1));
        assertThat(collectedTranslations.second(), containsInAnyOrder("one", "uno", "unu"));
    }

    @Test
    public void collectNoTuplesYieldsEmptyOptional() {
        Optional<Tuple<Integer, List<String>>> noTuple = Stream.<Tuple<Integer, Optional<String>>>empty().collect(toMultituple());
        assertThat(noTuple, OptionalMatchers.empty());
    }

    @Test
    public void collectMultitupleFromTuplesWithNonDistinctFirstElementIsErroneous() {
        NumTranslation oneInEnglish = () -> Tuple.of(1, Optional.of("one"));
        NumTranslation missingOne = () -> Tuple.of(1, Optional.empty());
        NumTranslation oneInEsperanto = () -> Tuple.of(1, Optional.of("unu"));
        NumTranslation twoInEnglish = () -> Tuple.of(2, Optional.of("two"));

        Stream<NumTranslation> translations = Stream.<NumTranslation>builder().add(oneInEnglish).add(missingOne).add(oneInEsperanto).add(twoInEnglish).build();
        expectedException.expect(ConflictingElementEncountered.class);
        translations.collect(toMultituple());
    }


    @Test
    @SuppressWarnings("deprecation")
    public void adaptACollector() {
        assertThat(Stream.of("1", "2", "3").collect(adapt(Collectors.<String>toList()).andThen(l -> l.get(0))), is("1"));
    }

    @Test
    public void collectTheValueOfSingleElementStream() {
        assertThat(Stream.of(42).collect(allowAtMostOne()), contains(is(42)));
        assertThat(Stream.empty().collect(allowAtMostOne()), OptionalMatchers.empty());
    }

    @Property
    public void allowAtMostOneFails(@When(satisfies = " #_.size() > 1") List<?> tooManyElements) {
        expectedException.expect(ViewableAsOptional.TooManyElements.class);
        tooManyElements.stream().parallel().collect(allowAtMostOne());
    }

    @Property
    public void allowAtMostOneFailsWithCustomException(@When(satisfies = " #_.size() > 1") List<?> tooManyElements) {
        expectedException.expect(IllegalStateException.class);
        tooManyElements.stream().collect(allowAtMostOneOrElseThrow((first, excess) -> {
            assertThat(first, is(tooManyElements.get(0)));
            assertThat(excess, is(tooManyElements.get(1)));
            return new IllegalStateException();
        }));
    }
}
