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

import no.digipost.collection.ConflictingElementEncountered;
import no.digipost.tuple.Tuple;
import no.digipost.tuple.ViewableAsTuple;
import no.digipost.util.ViewableAsOptional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quicktheories.core.Gen;
import uk.co.probablyfine.matchers.OptionalMatchers;

import java.io.IOException;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.stream.IntStream.range;
import static no.digipost.DiggBase.close;
import static no.digipost.DiggCollectors.allowAtMostOne;
import static no.digipost.DiggCollectors.allowAtMostOneOrElseThrow;
import static no.digipost.DiggCollectors.asSuppressedExceptionsOf;
import static no.digipost.DiggCollectors.toMultimap;
import static no.digipost.DiggCollectors.toMultituple;
import static no.digipost.DiggCollectors.toSingleExceptionWithSuppressed;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.quicktheories.QuickTheory.qt;
import static org.quicktheories.generators.SourceDSL.lists;
import static org.quicktheories.generators.SourceDSL.strings;
import static uk.co.probablyfine.matchers.Java8Matchers.where;
import static uk.co.probablyfine.matchers.Java8Matchers.whereNot;
import static uk.co.probablyfine.matchers.OptionalMatchers.contains;

@ExtendWith(MockitoExtension.class)
class DiggCollectorsTest {

    interface NumTranslation extends ViewableAsTuple<Integer, Optional<String>> {}

    @Test
    void collectTuplesIntoMap() {
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
    void collectMultitupleFromTuplesWithEqualFirstElement() {
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
    void collectNoTuplesYieldsEmptyOptional() {
        Optional<Tuple<Integer, List<String>>> noTuple = Stream.<Tuple<Integer, Optional<String>>>empty().collect(toMultituple());
        assertThat(noTuple, OptionalMatchers.empty());
    }

    @Test
    void collectMultitupleFromTuplesWithNonDistinctFirstElementIsErroneous() {
        NumTranslation oneInEnglish = () -> Tuple.of(1, Optional.of("one"));
        NumTranslation missingOne = () -> Tuple.of(1, Optional.empty());
        NumTranslation oneInEsperanto = () -> Tuple.of(1, Optional.of("unu"));
        NumTranslation twoInEnglish = () -> Tuple.of(2, Optional.of("two"));

        Stream<NumTranslation> translations = Stream.<NumTranslation>builder().add(oneInEnglish).add(missingOne).add(oneInEsperanto).add(twoInEnglish).build();
        assertThrows(ConflictingElementEncountered.class, () -> translations.collect(toMultituple()));
    }

    @Test
    void collectTheValueOfSingleElementStream() {
        assertThat(Stream.of(42).collect(allowAtMostOne()), contains(is(42)));
        assertThat(Stream.empty().collect(allowAtMostOne()), OptionalMatchers.empty());
        assertThat(Stream.of((String) null).collect(allowAtMostOne()), OptionalMatchers.empty());
    }

    @Test
    void allowAtMostOneFailsEvenIfExcessiveElementsAreNull() {
        Stream<String> xAndNull = Stream.of("x", null);
        assertThrows(ViewableAsOptional.TooManyElements.class, () -> xAndNull.collect(allowAtMostOne()));
    }

    @Test
    void convertTwoExceptionsToSingleWithSuppressed() {
        Exception mainException = new Exception("main");
        Exception suppressedException = new Exception("suppressed");
        Exception collatedException = Stream.of(mainException, suppressedException).collect(toSingleExceptionWithSuppressed()).get();
        assertThat(collatedException, is(mainException));
        assertThat(collatedException, where(Exception::getSuppressed, arrayContaining(suppressedException)));
    }

    @Test
    void convertLotsOfExceptionsToSingleWithTheRestSuppressed() {
        Stream<Exception> exceptions = range(0, 300).mapToObj(n -> new Exception("exception-" + n));
        Exception collatedException = exceptions.parallel().collect(toSingleExceptionWithSuppressed()).get();
        assertThat(collatedException, where(Throwable::getSuppressed, arrayWithSize(299)));
        assertThat(asList(collatedException.getSuppressed()), everyItem(where(Throwable::getSuppressed, emptyArray())));
    }

    @Test
    void addLotsOfSuppressedToGivenException() {
        Stream<Exception> exceptions = range(0, 300).mapToObj(n -> new Exception("exception-" + n));
        IOException collatedException = exceptions.parallel().collect(asSuppressedExceptionsOf(new IOException()));
        assertThat(collatedException, where(Throwable::getSuppressed, arrayWithSize(300)));
        assertThat(asList(collatedException.getSuppressed()), everyItem(where(Throwable::getSuppressed, emptyArray())));
    }

    @Test
    void suppressesExceptionsCorrectlyWithTryCatchBlocks(@Mock Connection connection, @Mock PreparedStatement pstmt) throws SQLException {
        SQLException connectionCloseException = new SQLException();
        BatchUpdateException statementCloseException = new BatchUpdateException();
        doThrow(connectionCloseException).when(connection).close();
        doThrow(statementCloseException).when(pstmt).close();

        try {
            throw new IllegalStateException("main");
        } catch (IllegalStateException e) {
            IllegalStateException collatedExceptions = close(pstmt, connection).collect(asSuppressedExceptionsOf(e));
            assertThat(collatedExceptions, sameInstance(e));
            assertThat(collatedExceptions, where(Throwable::getSuppressed, arrayContaining(statementCloseException, connectionCloseException)));
        }

    }

    @Test
    void convertNoExceptionsToEmptyOptional() {
        Optional<Exception> noException = range(0, 300).parallel().mapToObj(n -> new Exception("exception-" + n)).filter(e -> false).collect(toSingleExceptionWithSuppressed());
        assertThat(noException, whereNot(Optional::isPresent));
    }


    private final Gen<List<String>> listsWithAtLeastTwoElements = lists().of(strings().allPossible().ofLengthBetween(0, 10)).ofSizeBetween(2, 40);

    @Test
    void allowAtMostOneFails() {
        qt()
            .forAll(listsWithAtLeastTwoElements)
            .check(list -> {
                try {
                    list.stream().parallel().collect(allowAtMostOne());
                    return false;
                } catch (ViewableAsOptional.TooManyElements e) {
                    return true;
                }
            });
    }

    @Test
    void allowAtMostOneFailsWithCustomException() {
        IllegalStateException customException = new IllegalStateException();
        qt()
            .forAll(listsWithAtLeastTwoElements)
            .checkAssert(list -> {
                IllegalStateException thrown = assertThrows(IllegalStateException.class,
                        () -> list.stream().collect(allowAtMostOneOrElseThrow((first, excess) -> {
                            assertThat(first, is(list.get(0)));
                            assertThat(excess, is(list.get(1)));
                            return customException;
                        })));
                assertThat(thrown, sameInstance(customException));
            });
    }
}
