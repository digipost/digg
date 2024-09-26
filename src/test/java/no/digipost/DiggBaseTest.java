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

import no.digipost.util.AutoClosed;
import no.digipost.util.ThrowingAutoClosed;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quicktheories.WithQuickTheories;
import org.quicktheories.core.Gen;
import org.quicktheories.dsl.TheoryBuilder;
import uk.co.probablyfine.matchers.StreamMatchers;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.iterate;
import static java.util.stream.IntStream.rangeClosed;
import static java.util.stream.Stream.generate;
import static no.digipost.DiggBase.autoClose;
import static no.digipost.DiggBase.close;
import static no.digipost.DiggBase.forceOnAll;
import static no.digipost.DiggBase.friendlyName;
import static no.digipost.DiggBase.nonNull;
import static no.digipost.DiggBase.throwingAutoClose;
import static no.digipost.DiggCollectors.toSingleExceptionWithSuppressed;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.co.probablyfine.matchers.Java8Matchers.where;

@ExtendWith(MockitoExtension.class)
public class DiggBaseTest implements WithQuickTheories {

    @Test
    public void nonNullReferences() {
        TheoryBuilder<String> anyValue = qt()
            .forAll(strings().allPossible().ofLengthBetween(0, 100));

        anyValue.check(value -> nonNull("my value", value) == value);
        anyValue.check(value -> nonNull(value, d -> d) == value);
        anyValue.check(value -> nonNull("my value", value, NullPointerException::new) == value);
        anyValue.check(value -> nonNull(value, d -> d, d -> new NullPointerException()) == value);
    }

    @Test
    public void throwsNullPointerForNullReference() {
        assertThat(assertThrows(NullPointerException.class, () -> nonNull("my value", (Object) null)),
                where(Exception::getMessage, containsString("my value")));
    }

    @Test
    public void throwsCustomExceptionForNullReference() {
        assertThat(assertThrows(IllegalStateException.class, () -> nonNull("my value", (Object) null, IllegalStateException::new)),
                where(Exception::getMessage, containsString("my value")));
    }

    @Test
    public void throwsExceptionWithDescriptionInMessage() {
        String resourceName = "all/your/base/is/belong/to/us";
        assertThat(assertThrows(NullPointerException.class, () -> nonNull(resourceName, getClass()::getResource)),
                where(Exception::getMessage, containsString(resourceName)));
    }

    @Test
    public void extractOptionalValuesFromAnObject() {
        assertThat(DiggBase.<String, Character>extractIfPresent("abc",
                    s -> Optional.of(s.charAt(0)),
                    s -> Optional.empty(),
                    s -> Optional.of(s.charAt(2))),
                StreamMatchers.contains('a', 'c'));
        assertThat(DiggBase.<String, Character>extractIfPresent("abc",
                    s -> Optional.empty(),
                    s -> Optional.empty()),
                StreamMatchers.empty());
    }

    @Test
    public void extractValuesIncludesEverythingEvenNulls() {
        assertThat(DiggBase.<String, Character>extract("abc", s -> s.charAt(0), s -> null), StreamMatchers.contains('a', null));
    }

    @Test
    public void friendlyClassNameOfNonNestedClassIsJustTheSimpleName() {
        Gen<Class<?>> nonNestedClasses = arbitrary().pick(asList(String.class, InputStream.class, List.class, Integer.class));
        qt()
            .forAll(nonNestedClasses)
            .check(cls -> friendlyName(cls).equals(cls.getSimpleName()));
    }

    @Test
    public void includesNameOfAllClassesUpToTheFirstNonEnclosed() {
        assertThat(friendlyName(Base.StaticNested.class), is("Base.StaticNested"));
        assertThat(friendlyName(Base.StaticNested.O.Rama.class), is("Base.StaticNested.O.Rama"));
        assertThat(friendlyName(Base.Inner.class), is("Base.Inner"));

        class InMethod {}
        assertThat(friendlyName(InMethod.class), is(getClass().getSimpleName() + ".InMethod"));
    }

    @Test
    public void objectManagedByAutoCloseIsSameInstanceAsGiven() {
        Object object = new Object();
        ThrowingAutoClosed<Object, RuntimeException> managed = throwingAutoClose(object, o -> {});
        assertThat(managed.object(), sameInstance(object));
    }


    interface MyResource {
        void done();
    }

    @Test
    public void useArbitraryObjectWithTryWithResources(@Mock MyResource resource) {
        try (ThrowingAutoClosed<MyResource, RuntimeException> managedResource = throwingAutoClose(resource, MyResource::done)) {
            verifyNoInteractions(resource);
            managedResource.object(); //just to avoid javac lint warning
        }
        verify(resource, times(1)).done();
        verifyNoMoreInteractions(resource);
    }


    interface MyAutoCloseableResource extends AutoCloseable {
        void done() throws IOException;
        @Override
        void close() throws RuntimeException;
    }

    @Test
    public void wrappingAnAlreadyAutoCloseableWithAutoCloseWillAlsoInvokeClose(@Mock MyAutoCloseableResource resource) throws Exception {
        try (ThrowingAutoClosed<MyAutoCloseableResource, IOException> managedResource = throwingAutoClose(resource, MyAutoCloseableResource::done)) {
            verifyNoInteractions(resource);
            managedResource.object(); //just to avoid javac lint warning
        }
        InOrder inOrder = inOrder(resource);
        inOrder.verify(resource, times(1)).done();
        inOrder.verify(resource, times(1)).close();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void autoCloseWithoutCheckedException(@Mock MyResource resource) {
        try (AutoClosed<MyResource> managedResource = autoClose(resource, MyResource::done)) {
            verifyNoInteractions(resource);
            managedResource.object(); //just to avoid javac lint warning
        }
        verify(resource, times(1)).done();
        verifyNoMoreInteractions(resource);
    }

    @Test
    public void getAllExceptionsFromClosingSeveralAutoCloseables(@Mock AutoCloseable closeable) throws Exception {
        doNothing()
            .doThrow(new IOException())
            .doNothing()
            .doNothing()
            .doThrow(new IllegalStateException())
            .doNothing()
            .when(closeable).close();

        Stream<Exception> closeExceptionsStream = close(generate(() -> closeable).limit(5).toArray(AutoCloseable[]::new));
        verifyNoInteractions(closeable);
        List<Exception> closeExceptions = closeExceptionsStream.collect(toList());
        assertThat(closeExceptions, contains(asList(instanceOf(IOException.class), instanceOf(IllegalStateException.class))));
        verify(closeable, times(5)).close();
        verifyNoMoreInteractions(closeable);
    }

    @Nested
    @Timeout(4)
    class ForceOnAll {

        @Test
        void runsOperationOnMultipleElements() {
            List<Integer> consumed = new ArrayList<>();
            List<Exception> exceptions = forceOnAll(consumed::add, 1, 2, 3).collect(toList());
            assertThat(consumed, contains(1, 2, 3));
            assertThat(exceptions, empty());
        }

        @Test
        void exceptionsFromOperationAreCollected() {
            List<Integer> onlyEvenNumbers = new ArrayList<>();
            List<Exception> exceptions = forceOnAll(i -> {
                    if (i % 2 != 0) throw new IllegalArgumentException(i + " is odd!");
                    onlyEvenNumbers.add(i);
                }, 1, 2, 3, 4).collect(toList());
            assertThat(onlyEvenNumbers, contains(2, 4));
            assertThat(exceptions, contains(
                    where(Throwable::getMessage, is("1 is odd!")),
                    where(Throwable::getMessage, is("3 is odd!"))));
        }

        @Test
        void exceptionsFromTraversingStreamIsCollected() {
            List<Double> consumed = new ArrayList<>();
            List<Exception> exceptions = forceOnAll(consumed::add,
                    iterate(2, num -> num - 1).limit(5).mapToDouble(denominator -> 2 / denominator).boxed())
                    .collect(toList());

            assertAll(
                    () -> assertThat(exceptions, contains(isA(ArithmeticException.class))),
                    () -> assertThat(consumed, contains(1.0, 2.0, -2.0, -1.0)));
        }

        @Test
        void allElementsResolvedFromStreamException() {
            List<Exception> exceptions = forceOnAll(e -> fail("action should never be invoked"),
                    rangeClosed(1, 10).mapToObj(String::valueOf).map(s -> { throw new IllegalStateException(s); }))
                    .collect(toList());

            assertThat(exceptions, hasSize(10));
        }

        @Test
        void lastElementsResolvedFromStreamException() {
            List<Integer> consumed = new ArrayList<>();
            List<Exception> exceptions = forceOnAll(consumed::add,
                    iterate(2, i -> i + -1).limit(3).mapToObj(denominator -> 2 / denominator))
                    .collect(toList());
            assertAll(
                    () -> assertThat(exceptions, contains(isA(ArithmeticException.class))),
                    () -> assertThat(consumed, contains(1, 2)));
        }

        @Test
        void worksWithParalellStreams() {
            AtomicLong successes = new AtomicLong();
            long failures = forceOnAll(__ -> successes.incrementAndGet(),
                    iterate(0, i -> i + 1).limit(100_000).parallel()
                        .map(i -> i % 4) // 0, 1, 2, 3, 0, 1, 2, 3, 0, 1, 2, 3, ...
                        .map(denominator -> 4 / denominator) // Fails with div by zero 1/4 of the times
                        .boxed())
                .count();

            assertThat("3 times as much successes as failures: " + successes + " / " + failures,
                    failures * 3, is(successes.get()));
        }

        @Test
        void doesNotInvokeOperationOnNulls(@Mock AutoCloseable resource) throws Exception {
            Optional<RuntimeException> exception = forceOnAll(AutoCloseable::close, resource, null, resource, null, resource)
                    .collect(toSingleExceptionWithSuppressed())
                    .map(DiggExceptions::asUnchecked);
            assertAll(
                    () -> verify(resource, times(3)).close(),
                    () -> assertDoesNotThrow(() -> exception.ifPresent(e -> { throw e; })));
        }

    }

}


class Base {
    static class StaticNested {
        static class O {
            static class Rama {}
        }
    }
    class Inner {}
}
