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

import no.digipost.concurrent.OneTimeToggle;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toList;
import static no.digipost.DiggExceptions.applyUnchecked;
import static no.digipost.DiggExceptions.asUnchecked;
import static no.digipost.DiggExceptions.causalChainOf;
import static no.digipost.DiggExceptions.getUnchecked;
import static no.digipost.DiggExceptions.mayThrow;
import static no.digipost.DiggExceptions.runUnchecked;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.co.probablyfine.matchers.Java8Matchers.where;

public class DiggExceptionsTest {

    @Test
    public void causalChainOfNullIsEmptyStream() {
        assertThat(causalChainOf(null).collect(toList()), empty());
    }

    @Test
    public void returnsTheCausalChainOfExceptions() {
        List<Throwable> exception = causalChainOf(new Exception(new IllegalStateException(new IOException()))).collect(toList());
        assertThat(exception, contains(instanceOf(Exception.class), instanceOf(IllegalStateException.class), instanceOf(IOException.class)));
    }

    @Test
    public void runAThrowingRunnableUnchecked() {
        OneTimeToggle toggled = new OneTimeToggle();
        DiggExceptions.runUnchecked(() -> toggled.nowOrIfAlreadyThenThrow(() -> new AssertionError("should not be run twice!")));
        assertThat(toggled, where(OneTimeToggle::yet));

        Exception e = new Exception();
        assertThat(assertThrows(RuntimeException.class, () -> runUnchecked(() -> { throw e; })), where(Exception::getCause, sameInstance(e)));
    }

    @Test
    public void getAThrowingSupplierUnchecked() {
        assertThat(getUnchecked(() -> 42), is(42));

        Exception e = new Exception();
        assertThat(assertThrows(RuntimeException.class, () -> getUnchecked(() -> { throw e; })), where(Exception::getCause, sameInstance(e)));
    }

    @Test
    public void applyAThrowingFunctionUnchecked() {
        assertThat(applyUnchecked(Math::round, 4.6f), is(5));
        assertThat(applyUnchecked(n -> n, null), nullValue());
        assertThat(applyUnchecked(n -> n, Optional.empty()), is(Optional.empty()));

        Exception e = new Exception();
        assertThat(assertThrows(RuntimeException.class, () -> applyUnchecked(t -> { throw e; }, "anything")), where(Exception::getCause, sameInstance(e)));
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

        assertThat(assertThrows(Exception.class, () -> mayThrow((t, u) -> { if (t == null) throw ex; }).accept(null, null)), sameInstance(ex));
    }


    @Nested
    class AsUnchecked {

        @Test
        void castToUnchecked() {
            Exception e = new IllegalStateException();
            assertThat(e, where(DiggExceptions::asUnchecked, sameInstance(e)));
        }

        @Test
        void UnknownCheckedExceptionBecomesRuntimeException() {
            class MyCheckedException extends Exception {
                public MyCheckedException() {
                    super("Who in their right mind would define their own checked exception");
                }
            }
            RuntimeException uncheckedException = asUnchecked(new MyCheckedException());
            assertAll(
                    () -> assertThat(uncheckedException, where(Object::getClass, is(RuntimeException.class))),
                    () -> assertThat(uncheckedException, where(Exception::getMessage, containsString("DiggExceptionsTest.AsUnchecked.MyCheckedException"))),
                    () -> assertThat(uncheckedException, where(Exception::getMessage, containsString("Who in their right mind"))));
        }

        @Test
        void ioExceptionAsUncheckedIOException() {
            assertThat(new IOException("error"), where(DiggExceptions::asUnchecked, instanceOf(UncheckedIOException.class)));
        }

    }

}
