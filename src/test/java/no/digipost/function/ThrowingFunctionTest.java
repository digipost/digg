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
package no.digipost.function;

import org.junit.jupiter.api.Test;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static java.util.Optional.empty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ThrowingFunctionTest {

    private final RuntimeException ex = new RuntimeException("fail");

    private final Error err = new Error();

    @Test
    public void rethrowOriginalRuntimeException() {
        ThrowingFunction<Integer, ?, Exception> fn = i -> {throw ex;};
        assertThat(assertThrows(RuntimeException.class, () -> fn.asUnchecked().apply(42)), sameInstance(ex));
    }

    @Test
    public void handlesNullResult() {
        ThrowingFunction<String, String, ?> fn = i -> null;
        assertThat(fn.asUnchecked().apply("x"), nullValue());
    }

    @Test
    public void rethrowOriginalError() {
        ThrowingFunction<Integer, ?, Exception> fn = i -> {throw err;};
        assertThat(assertThrows(Error.class, () -> fn.asUnchecked().apply(42)), sameInstance(err));
    }

    @Test
    public void translateToEmptyOptionalAndDelegateExceptionToHandler() {
        ThrowingFunction<Integer, ?, Exception> fn = i -> {throw ex;};

        @SuppressWarnings("unchecked")
        Consumer<Exception> getException = mock(Consumer.class);
        assertThat(fn.ifException(getException).apply(42), is(empty()));
        verify(getException, times(1)).accept(ex);

        @SuppressWarnings("unchecked")
        BiConsumer<Integer, Exception> getValueAndException = mock(BiConsumer.class);
        assertThat(fn.ifException(getValueAndException).apply(42), is(empty()));
        verify(getValueAndException, times(1)).accept(42, ex);
    }

    @Test
    public void mapThrownExceptionToResultValue() {
        ThrowingFunction<Integer, String, Exception> fn = i -> {throw ex;};

        assertThat(fn.ifExceptionApply(Exception::getMessage).apply(42), is("fail"));
    }

    @Test
    public void composeFunctions() throws Exception {
        ThrowingFunction<Character, String, Exception> toString = c -> c.toString();
        assertThat(toString.andThen(String::toUpperCase).apply('c'), is("C"));

        ThrowingBiFunction<Character, Character, String, Exception> join = (c1, c2) -> c1.toString() + c2;
        assertThat(join.andThen(String::toUpperCase).apply('a', 'b'), is("AB"));
    }

}
