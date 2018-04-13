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
import com.pholser.junit.quickcheck.When;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import no.digipost.util.AutoClosed;
import no.digipost.util.ThrowingAutoClosed;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InOrder;

import java.io.IOException;
import java.util.Optional;

import static co.unruly.matchers.StreamMatchers.contains;
import static co.unruly.matchers.StreamMatchers.empty;
import static no.digipost.DiggBase.autoClose;
import static no.digipost.DiggBase.friendlyName;
import static no.digipost.DiggBase.nonNull;
import static no.digipost.DiggBase.throwingAutoClose;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(JUnitQuickcheck.class)
public class DiggBaseTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Property
    public void yieldsSameInstanceOnNonNullReferences(String value) {
        assertThat(nonNull("my value", value), sameInstance(value));
        assertThat(nonNull(value, d -> d), sameInstance(value));
        assertThat(DiggBase.<String, NullPointerException>nonNull("my value", value, NullPointerException::new), sameInstance(value));
        assertThat(DiggBase.<String, NullPointerException>nonNull(value, d -> d, d -> new NullPointerException()), sameInstance(value));
    }

    @Test
    public void throwsNullPointerForNullReference() {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("my value");
        nonNull("my value", (Object) null);
    }

    @Test
    public void throwsCustomExceptionForNullReference() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("my value");
        nonNull("my value", (Object) null, IllegalStateException::new);
    }

    @Test
    public void throwsExceptionWithDescriptionInMessage() {
        String resourceName = "all/your/base/is/belong/to/us";
        expectedException.expectMessage(resourceName);
        nonNull(resourceName, getClass()::getResource);
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

    @Property
    public void friendlyClassNameIsJustTheSimpleName(@When(satisfies = "#_ != null") Object anyInstanceOfNonNestedClass) {
        Class<?> anyNonNestedClass = anyInstanceOfNonNestedClass.getClass();
        assumeThat(anyNonNestedClass.getEnclosingClass(), nullValue());
        assertThat(friendlyName(anyNonNestedClass), is(anyNonNestedClass.getSimpleName()));
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
            verifyZeroInteractions(resource);
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
            verifyZeroInteractions(resource);
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
            verifyZeroInteractions(resource);
        }
        verify(resource, times(1)).done();
        verifyNoMoreInteractions(resource);
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
