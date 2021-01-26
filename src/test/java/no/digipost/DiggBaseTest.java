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
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.quicktheories.WithQuickTheories;
import org.quicktheories.core.Gen;
import org.quicktheories.dsl.TheoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static uk.co.probablyfine.matchers.Java8Matchers.where;
import static uk.co.probablyfine.matchers.StreamMatchers.contains;
import static uk.co.probablyfine.matchers.StreamMatchers.empty;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.generate;
import static no.digipost.DiggBase.autoClose;
import static no.digipost.DiggBase.close;
import static no.digipost.DiggBase.friendlyName;
import static no.digipost.DiggBase.nonNull;
import static no.digipost.DiggBase.throwingAutoClose;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

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
        assertThat(DiggBase.<String, Character>extractIfPresent("abc", s -> Optional.of(s.charAt(0)), s -> Optional.empty(), s -> Optional.of(s.charAt(2))), contains('a', 'c'));
        assertThat(DiggBase.<String, Character>extractIfPresent("abc", s -> Optional.empty(), s -> Optional.empty()), empty());
    }

    @Test
    public void extractValuesIncludesEverythingEvenNulls() {
        assertThat(DiggBase.<String, Character>extract("abc", s -> s.charAt(0), s -> null), contains('a', null));
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


    @Test
    public void useArbitraryObjectWithTryWithResources() {
        abstract class MyResource {
            abstract void done();
        }
        MyResource resource = mock(MyResource.class);
        try (ThrowingAutoClosed<MyResource, RuntimeException> managedResource = throwingAutoClose(resource, MyResource::done)) {
            verifyNoInteractions(resource);
        }
        verify(resource, times(1)).done();
        verifyNoMoreInteractions(resource);
    }

    @Test
    public void wrappingAnAlreadyAutoCloseableWithAutoCloseWillAlsoInvokeClose() throws Exception {
        abstract class MyResource implements AutoCloseable {
            abstract void done() throws IOException;
        }
        MyResource resource = mock(MyResource.class);
        try (ThrowingAutoClosed<MyResource, IOException> managedResource = throwingAutoClose(resource, MyResource::done)) {
            verifyNoInteractions(resource);
        }
        InOrder inOrder = inOrder(resource);
        inOrder.verify(resource, times(1)).done();
        inOrder.verify(resource, times(1)).close();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void autoCloseWithoutCheckedException() {
        abstract class MyResource {
            abstract void done();
        }
        MyResource resource = mock(MyResource.class);
        try (AutoClosed<MyResource> managedResource = autoClose(resource, MyResource::done)) {
            verifyNoInteractions(resource);
        }
        verify(resource, times(1)).done();
        verifyNoMoreInteractions(resource);
    }

    @Test
    public void getAllExceptionsFromClosingSeveralAutoCloseables() throws Exception {
        AutoCloseable closeable = mock(AutoCloseable.class);
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
        assertThat(closeExceptions, Matchers.contains(asList(instanceOf(IOException.class), instanceOf(IllegalStateException.class))));
        verify(closeable, times(5)).close();
        verifyNoMoreInteractions(closeable);
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
