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
package no.digipost.exceptions;

import no.digipost.concurrent.OneTimeToggle;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toList;
import static no.digipost.exceptions.Exceptions.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ExceptionsTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();


    @Test
    public void causalChainOfNullIsEmptyStream() {
        assertThat(causalChainOf(null).collect(toList()), empty());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void returnsTheCausalChainOfExceptions() {
        List<Throwable> exception = causalChainOf(new Exception(new IllegalStateException(new IOException()))).collect(toList());
        assertThat(exception, contains(instanceOf(Exception.class), instanceOf(IllegalStateException.class), instanceOf(IOException.class)));
    }

    @Test
    public void runAThrowingRunnableUnchecked() {
        OneTimeToggle toggled = new OneTimeToggle();
        Exceptions.runUnchecked(() -> toggled.nowOrIfAlreadyThenThrow(() -> new AssertionError("should not be run twice!")));
        assertTrue(toggled.yet());

        Exception e = new Exception();
        try {
            runUnchecked(() -> { throw e; });
        } catch (RuntimeException ex) {
            assertThat(ex.getCause(), sameInstance(e));
            return;
        }
        fail("Should throw exception");
    }

    @Test
    public void getAThrowingSupplierUnchecked() {
        assertThat(getUnchecked(() -> 42), is(42));

        Exception e = new Exception();
        try {
            getUnchecked(() -> { throw e; });
        } catch (RuntimeException ex) {
            assertThat(ex.getCause(), sameInstance(e));
            return;
        }
        fail("Should throw exception");
    }

    @Test
    public void applyAThrowingFunctionUnchecked() {
        assertThat(applyUnchecked(Math::round, 4.6f), is(5));

        Exception e = new Exception();
        try {
            applyUnchecked(t -> { throw e; }, "anything");
        } catch (RuntimeException ex) {
            assertThat(ex.getCause(), sameInstance(e));
            return;
        }
        fail("Should throw exception");
    }

    @Test
    public void factoryMethodsForThrowingFunctionalInterfaces() throws Throwable {
        assertThat(mayThrow((t) -> t).apply("a"), is("a"));
        assertThat(mayThrow((t, u) -> t).apply("a", "b"), is("a"));
        assertThat(mayThrow(() -> "a").get(), is("a"));

        Exception ex = new Exception();
        @SuppressWarnings("unchecked")
        Consumer<Exception> exceptionHandler = mock(Consumer.class);
        mayThrow(t -> { if (t == null) throw ex; }).ifException(exceptionHandler).accept(null);
        verify(exceptionHandler).accept(ex);

        expectedException.expect(sameInstance(ex));
        mayThrow((t, u) -> { if (t == null) throw ex; }).accept(null, null);
    }




}
